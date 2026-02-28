# Map-Reduce with Child Workflows

## What

Use Temporal **child workflows** to fan out work items in parallel, then collect results in the parent workflow. The parent acts as the orchestrator (map step), and each child processes one item or chunk independently (reduce step).

## Why

Temporal activities have execution time limits and no built-in parallelism within a single workflow. Child workflows run independently: each has its own execution history, retry policy, and can be monitored in the Temporal UI. If a child fails, the parent can decide whether to retry, skip, or abort the entire batch.

## How

The parent workflow chunks the input list and starts a child workflow per chunk using `Async.function()`. It waits for all children with `Promise.allOf()`, then collects and aggregates the results.

Each child workflow processes its chunk sequentially, calling activities for each item (validate, charge, fulfill). Activities contain the actual side effects.

## Key Considerations

- **Control parallelism** — do not start 10,000 child workflows at once. Batch into chunks.
- **Deterministic child workflow IDs** — derive from parent ID + chunk index. Enables idempotent replays.
- **Set timeouts on children** — use `WorkflowExecutionTimeout`. Without it, a stuck child blocks the parent indefinitely.
- **Handle partial failures** — decide upfront: fail the entire batch or collect partial results.

---

## Code

### Parent Workflow — Fan Out and Aggregate

```kotlin
class BatchProcessWorkflowImpl : BatchProcessWorkflow {

    override fun processBatch(request: BatchRequest): BatchResult {
        val parentWorkflowId = Workflow.getInfo().workflowId
        val chunks = request.orderIds.chunked(request.chunkSize)

        // fan out: start a child workflow for each chunk
        val childPromises = chunks.mapIndexed { index, chunk ->
            val childOptions = ChildWorkflowOptions.newBuilder()
                .setWorkflowId("${parentWorkflowId}-chunk-${index}")  // deterministic ID
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build()

            val child = Workflow.newChildWorkflowStub(
                ProcessOrderWorkflow::class.java, childOptions
            )

            Async.function(child::processOrders, chunk)
        }

        // wait for all children
        Promise.allOf(childPromises).get()

        // reduce: collect and aggregate
        val allResults = childPromises.flatMap { it.get() }
        return BatchResult(
            totalOrders = allResults.size,
            successCount = allResults.count { it.success },
            failureCount = allResults.count { !it.success },
            results = allResults,
        )
    }
}
```

### Child Workflow — Process a Chunk

```kotlin
class ProcessOrderWorkflowImpl : ProcessOrderWorkflow {

    private val activities = Workflow.newActivityStub(
        OrderActivities::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
            .build()
    )

    override fun processOrders(orderIds: List<String>): List<OrderResult> {
        return orderIds.map { orderId ->
            try {
                activities.validateOrder(orderId)
                activities.chargePayment(orderId)
                activities.fulfillOrder(orderId)
            } catch (e: Exception) {
                OrderResult(orderId, success = false, message = e.message ?: "Unknown error")
            }
        }
    }
}
```

### Activities — Side Effects

```kotlin
@ActivityInterface
interface OrderActivities {
    @ActivityMethod fun validateOrder(orderId: String): OrderResult
    @ActivityMethod fun chargePayment(orderId: String): OrderResult
    @ActivityMethod fun fulfillOrder(orderId: String): OrderResult
}
```

### Temporal Configuration

```kotlin
@Configuration
class TemporalConfig {

    @Bean
    fun workflowClient(): WorkflowClient {
        val stubs = WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder().setTarget("localhost:7233").build()
        )
        return WorkflowClient.newInstance(stubs,
            WorkflowClientOptions.newBuilder().setNamespace("default").build())
    }

    @Bean(destroyMethod = "shutdown")
    fun workerFactory(client: WorkflowClient): WorkerFactory {
        val factory = WorkerFactory.newInstance(client)
        val worker = factory.newWorker("batch-processing")
        worker.registerWorkflowImplementationTypes(
            BatchProcessWorkflowImpl::class.java,
            ProcessOrderWorkflowImpl::class.java,
        )
        worker.registerActivitiesImplementations(OrderActivitiesImpl())
        factory.start()
        return factory
    }
}
```
