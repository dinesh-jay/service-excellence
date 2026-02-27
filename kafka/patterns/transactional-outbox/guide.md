# Transactional Outbox

## What

Write domain events to an **outbox table** in the same database transaction as the business operation. A separate process reads unpublished rows and publishes them to Kafka.

## Why

Dual writes — saving to the database and publishing to Kafka — are not atomic. If the Kafka publish fails after the DB commit, you lose the event. If you publish first and the DB write fails, you have a phantom event with no backing state. The outbox pattern eliminates this by making event capture part of the database transaction.

## How

Create an outbox table:

```
outbox_events
├── id (UUID, PK)
├── aggregate_type (string)
├── aggregate_id (string)
├── event_type (string)
├── payload (JSON text)
├── created_at (timestamp)
└── published_at (timestamp, nullable)
```

In your service method, persist the business entity and insert an outbox row in the same `@Transactional` block. A publisher component picks up unpublished rows and sends them to Kafka.

**Two approaches:**

1. **Polling publisher** — a `@Scheduled` job queries `WHERE published_at IS NULL`, publishes to Kafka, then marks rows as published. Simple, reliable, good enough for most use cases. Latency is bounded by the polling interval.

2. **CDC-based** (Debezium) — streams the outbox table's WAL directly to Kafka. Lower latency, no polling overhead, but adds operational complexity (connector management, schema changes).

**Recommendation:** Start with polling. Move to CDC when sub-second latency matters.

## Key Detail

Do not delete outbox rows after publishing. Mark them as published (`published_at = now()`). This preserves an audit trail and enables replay during incident recovery. Archive or purge old rows on a schedule.

## See Also

- [Runnable example](./example/) — polling-based outbox with Spring Boot 4
- [Anti-patterns](./anti-patterns.md) — common mistakes
