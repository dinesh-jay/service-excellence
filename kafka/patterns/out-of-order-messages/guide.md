# Out-of-Order Messages

## What

Handling messages that arrive in a different sequence than they were logically produced. Kafka guarantees ordering only within a partition — across partitions and during retries or rebalances, order is not guaranteed.

## Why

Events for the same entity (e.g., OrderCreated, OrderUpdated, OrderShipped) may arrive out of sequence. Processing OrderShipped before OrderCreated corrupts state. Ignoring ordering leads to silent data inconsistency that is expensive to detect and fix.

## How

Three strategies, progressively more complex:

**1. Partition key strategy** — route events for the same aggregate to the same partition using a consistent key (e.g., `orderId`). This is the simplest approach and handles most cases. If all events for an entity go to one partition, Kafka's per-partition ordering guarantee does the work.

**2. Sequence-number reordering** — each event carries a monotonic sequence number per aggregate. The consumer tracks `lastProcessedSequence` per entity:

```
seq == expected  → process, advance state, drain buffer
seq >  expected  → buffer in a pending table, ack the offset
seq <  expected  → duplicate/stale, skip
```

After processing the expected event, the consumer drains buffered events that are now contiguous. A scheduled background job retries buffered events periodically as a safety net.

**3. Timestamp-based last-write-wins** — compare the event timestamp with the stored entity's `lastUpdatedAt`. Apply only if the event is newer. Suitable for CDC-style idempotent updates where "latest value" is good enough. Not suitable for strict sequential processing because clock skew between producers makes timestamp ordering unreliable.

**Recommendation:** Start with partition keys. Add sequence-number reordering only when you have multiple producers for the same entity or cross-partition ordering requirements.

## Key Configuration

```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false
      auto-offset-reset: earliest
      isolation-level: read_committed
    listener:
      ack-mode: manual
```

Manual commit ensures buffered events do not lose their offset. The consumer acknowledges immediately after buffering — it does not hold the partition waiting for the gap to fill.

## See Also

- [Runnable example](./example/) — sequence-number reordering with Spring Boot 4
- [Anti-patterns](./anti-patterns.md) — common mistakes
