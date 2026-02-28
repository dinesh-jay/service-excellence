# Map-Reduce with Child Workflows

## What

Use Temporal **child workflows** to fan out work items in parallel, then collect results in the parent workflow. The parent acts as the orchestrator (map step), and each child processes one item or chunk independently (reduce step).

## Why

Temporal activities have execution time limits and no built-in parallelism within a single workflow. Running long-running parallel work as activities leads to timeouts, exhausted worker thread pools, and no independent visibility into individual items.

Child workflows run independently: each has its own execution history, retry policy, and can be monitored in the Temporal UI. If a child fails, the parent can decide whether to retry, skip, or abort the entire batch. This is ideal for batch processing — process 1,000 orders, generate reports for each region, transform data in chunks.

## How

1. The parent workflow receives a list of work items.
2. The parent partitions items into chunks (to control parallelism).
3. For each chunk, the parent starts a child workflow using `Async.function()`.
4. Each child workflow processes its items by calling activities (validate, transform, persist).
5. The parent calls `Promise.allOf()` to wait for all children, then collects results.
6. The parent aggregates the results and returns a summary.

```
Parent Workflow
├── Chunk 1 → Child Workflow → Activities (validate, charge, fulfill)
├── Chunk 2 → Child Workflow → Activities (validate, charge, fulfill)
└── Chunk 3 → Child Workflow → Activities (validate, charge, fulfill)
└── Aggregate results
```

## Key Considerations

- **Control parallelism** — do not start 10,000 child workflows at once. Batch items into chunks (e.g., 50 children at a time) to avoid overwhelming the Temporal server and workers.
- **Deterministic child workflow IDs** — derive the child workflow ID from the parent workflow ID and the chunk index (e.g., `${parentId}-chunk-${index}`). This enables idempotent retries: if the parent restarts, it reconnects to existing children rather than spawning duplicates.
- **Set timeouts on children** — use `WorkflowExecutionTimeout` on child workflow options. Without it, a stuck child blocks the parent indefinitely.
- **Handle partial failures** — decide your policy upfront: fail the entire batch if any child fails, or collect partial results and report failures. The example demonstrates the latter.

## See Also

- [Runnable example](./example/) — Spring Boot 4 batch order processing
- [Anti-patterns](./anti-patterns.md) — common mistakes
