package com.example.mapreduce

import io.temporal.workflow.Async
import io.temporal.workflow.ChildWorkflowOptions
import io.temporal.workflow.Promise
import io.temporal.workflow.Workflow
import java.time.Duration

class BatchProcessWorkflowImpl : BatchProcessWorkflow {

    private val log = Workflow.getLogger(BatchProcessWorkflowImpl::class.java)

    override fun processBatch(request: BatchRequest): BatchResult {
        val parentWorkflowId = Workflow.getInfo().workflowId
        val chunks = request.orderIds.chunked(request.chunkSize)

        log.info(
            "Starting batch processing: {} orders in {} chunks",
            request.orderIds.size, chunks.size
        )

        // Fan out: start a child workflow for each chunk
        val childPromises = chunks.mapIndexed { index, chunk ->
            val childOptions = ChildWorkflowOptions.newBuilder()
                .setWorkflowId("${parentWorkflowId}-chunk-${index}")
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build()

            val child = Workflow.newChildWorkflowStub(
                ProcessOrderWorkflow::class.java,
                childOptions
            )

            Async.function(child::processOrders, chunk)
        }

        // Wait for all children to complete
        Promise.allOf(childPromises).get()

        // Reduce: collect and aggregate results
        val allResults = childPromises.flatMap { it.get() }
        val successCount = allResults.count { it.success }
        val failureCount = allResults.count { !it.success }

        log.info(
            "Batch complete: {} succeeded, {} failed out of {} total",
            successCount, failureCount, allResults.size
        )

        return BatchResult(
            totalOrders = allResults.size,
            successCount = successCount,
            failureCount = failureCount,
            results = allResults,
        )
    }
}
