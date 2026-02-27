# Idempotent Consumer

## What

Processing the same Kafka message multiple times produces the same result. The consumer detects duplicates and skips reprocessing.

## Why

Kafka guarantees at-least-once delivery. Duplicates happen during consumer group rebalances, producer retries (`acks=all` + network timeout), and crash recovery. If your consumer creates an order on every message, a duplicate creates two orders.

## How

Use a **deduplication store** — a database table with a unique constraint on the message identifier.

```
processed_messages
├── message_id (PK, unique)
├── processed_at (timestamp)
```

For every incoming message:

1. Extract the message ID (from a Kafka header or a field in the payload — prefer headers).
2. Check the dedup table. If the ID exists, skip.
3. Process the business logic and insert into the dedup table **in the same database transaction**.
4. Manually commit the Kafka offset.

Wrapping steps 2-4 in a single `@Transactional` block ensures atomicity between your business state and the dedup record.

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

- `enable-auto-commit=false` — you control when offsets are committed.
- `ack-mode=manual` — acknowledge after successful processing.
- `isolation-level=read_committed` — only read messages from committed transactions (relevant when producers use exactly-once).

## See Also

- [Runnable example](./example/) — Spring Boot 4 + JPA deduplication
- [Anti-patterns](./anti-patterns.md) — common mistakes
