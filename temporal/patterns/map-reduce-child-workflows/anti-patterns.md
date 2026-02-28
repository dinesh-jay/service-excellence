# Map-Reduce with Child Workflows — Anti-Patterns

## 1. Using Activities Instead of Child Workflows for Long-Running Parallel Work

**What people do:** Execute all parallel work items as activities within a single workflow, often using `Async.function()` on activity stubs to run them concurrently.

**Why it fails:** Activities have a `ScheduleToCloseTimeout` (typically minutes, not hours). Long-running items exceed this timeout and fail. Activity retries restart from scratch — there is no checkpointing. All activities share the worker's activity thread pool; saturating it blocks other workflows. You cannot individually inspect, retry, or cancel a specific item in the Temporal UI — activities lack their own execution history.

**Instead:** Use child workflows for each work item or chunk. Child workflows have their own execution history, can run for arbitrarily long durations, retry independently, and are visible as separate executions in the Temporal UI.

## 2. Unbounded Fan-Out

**What people do:** Start one child workflow per item — 10,000 items means 10,000 simultaneous child workflow starts.

**Why it fails:** Temporal server tracks each child as an event in the parent's execution history. Thousands of concurrent children generate massive history, potentially hitting the 50,000-event history size limit. The burst of `StartChildWorkflowExecution` commands overwhelms the Temporal server's task processing. Workers may also exhaust memory tracking thousands of pending promises.

**Instead:** Batch items into chunks (e.g., 50-100 items per child workflow). For very large batches, use a sliding window pattern: start N children, as each completes start the next, keeping at most N running concurrently.

## 3. Non-Deterministic Child Workflow IDs

**What people do:** Generate child workflow IDs using `UUID.randomUUID()` or let Temporal auto-assign IDs.

**Why it fails:** If the parent workflow replays (due to worker restart, deployment, or crash), a non-deterministic ID generates a new UUID on replay. This starts a *new* child instead of reconnecting to the already-running one. The result: duplicate work, duplicate side effects, and orphaned child workflows that nobody cancels.

**Instead:** Derive child workflow IDs deterministically from the parent workflow ID and the item identifier: `"${parentWorkflowId}-order-${orderId}"`. On replay, the same ID reconnects to the existing child execution.
