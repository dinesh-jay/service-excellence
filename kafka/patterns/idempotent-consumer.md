# Idempotent Consumer

## What

Processing the same Kafka message multiple times produces the same result. The consumer detects duplicates and skips reprocessing.

## Why

Kafka guarantees at-least-once delivery. Duplicates happen during consumer group rebalances, producer retries (`acks=all` + network timeout), and crash recovery. If your consumer creates an order on every message, a duplicate creates two orders.

## How

Use a **deduplication store** — a database table with a unique constraint on the message identifier.

```sql
CREATE TABLE processed_messages (
    message_id  VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

For every incoming message:

1. Extract the message ID (from a Kafka header or a field in the payload — prefer headers).
2. Check the dedup table. If the ID exists, skip.
3. Process the business logic and insert into the dedup table **in the same database transaction**.
4. Manually commit the Kafka offset.

```kotlin
@KafkaListener(topics = ["orders"])
@Transactional
fun consume(record: ConsumerRecord<String, OrderEvent>, ack: Acknowledgment) {
    val messageId = record.headers().lastHeader("message_id")
        ?.let { String(it.value()) }
        ?: "${record.topic()}-${record.partition()}-${record.offset()}"

    if (processedMessageRepository.existsById(messageId)) {
        log.info("Duplicate detected, skipping: {}", messageId)
        ack.acknowledge()
        return
    }

    // --- business logic ---
    processOrder(record.value())

    // mark as processed in the same transaction
    processedMessageRepository.save(ProcessedMessage(messageId))
    ack.acknowledge()
}
```

## Key Configuration

```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false        # you control when offsets commit
      auto-offset-reset: earliest
      isolation-level: read_committed  # only read committed transactional messages
    listener:
      ack-mode: manual                 # acknowledge after successful processing
```
