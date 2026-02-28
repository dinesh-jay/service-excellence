# Temporal Operational Excellence

Metrics, visibility, and health checks for production Temporal workloads.

## Metrics

Temporal server exposes Prometheus metrics. Key metrics to monitor:

| Metric | What It Tells You |
|--------|-------------------|
| `temporal_workflow_task_schedule_to_start_latency` | How long workflow tasks wait in the queue. High = workers overloaded. |
| `temporal_activity_schedule_to_start_latency` | Activity task queue backlog. High = not enough activity workers. |
| `temporal_workflow_endtoend_latency` | Total workflow execution time. |
| `temporal_workflow_failed` | Failed workflow count. Should be near zero in steady state. |
| `temporal_activity_execution_failed` | Failed activity count after exhausting retries. |

### SDK Metrics

Enable Micrometer metrics in the Temporal SDK:

```kotlin
val stubs = WorkflowServiceStubs.newServiceStubs(
    WorkflowServiceStubsOptions.newBuilder()
        .setTarget("localhost:7233")
        .setMetricsScope(meterRegistry)  // Micrometer registry
        .build()
)
```

### Alerting Rules

- Workflow task schedule-to-start latency > 5s — workers cannot keep up.
- Activity schedule-to-start latency > 30s — scale activity workers.
- Failed workflow rate > 0 — investigate root cause.
- Workflow execution time > expected SLA — child workflows or activities are slow.

## Health Checks

```kotlin
@Component
class TemporalHealthIndicator(
    private val workflowServiceStubs: WorkflowServiceStubs,
) : HealthIndicator {

    override fun health(): Health {
        return try {
            workflowServiceStubs.blockingStub()
                .getSystemInfo(GetSystemInfoRequest.getDefaultInstance())
            Health.up().build()
        } catch (e: Exception) {
            Health.down(e).build()
        }
    }
}
```

## Temporal UI

The Temporal UI (`temporalio/ui`) provides:
- Workflow execution list with status filters
- Detailed execution history (every event: activity scheduled, completed, failed)
- Child workflow navigation from parent
- Signal and query interfaces for running workflows
- Workflow termination and cancellation

Run locally: `docker run -p 8080:8080 -e TEMPORAL_ADDRESS=host.docker.internal:7233 temporalio/ui`

## Logging Best Practices

- Use `Workflow.getLogger()` inside workflows — not `LoggerFactory`. Temporal's logger is replay-aware and suppresses duplicate log entries during replay.
- Log at activity boundaries: start, success, failure.
- Include workflow ID and run ID in structured log fields for correlation.
