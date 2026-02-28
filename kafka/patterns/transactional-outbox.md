# Transactional Outbox

## What

Write domain events to an **outbox table** in the same database transaction as the business operation. A separate process reads unpublished rows and publishes them to Kafka.

## Why

Dual writes — saving to the database and publishing to Kafka — are not atomic. If the Kafka publish fails after the DB commit, you lose the event. If you publish first and the DB write fails, you have a phantom event with no backing state.

## How

Create an outbox table:

```sql
CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        TEXT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMP NULL
);
```

In your service method, persist the business entity and insert an outbox row in the same `@Transactional` block:

```kotlin
@Transactional
fun createOrder(customerId: String, amount: Double): Order {
    val order = orderRepository.save(Order(customerId = customerId, amount = amount))

    outboxRepository.save(OutboxEvent(
        aggregateType = "Order",
        aggregateId = order.id,
        eventType = "OrderCreated",
        payload = objectMapper.writeValueAsString(order),
    ))

    return order
}
```

A poller picks up unpublished rows and sends them to Kafka:

```kotlin
@Scheduled(fixedDelay = 5000)
@Transactional
fun publishPendingEvents() {
    val events = outboxRepository.findByPublishedAtIsNull()

    for (event in events) {
        kafkaTemplate.send("${event.aggregateType.lowercase()}-events", event.aggregateId, event.payload)
        event.publishedAt = Instant.now()
        outboxRepository.save(event)
    }
}
```

**Two approaches:** polling-based (simple, good enough for most) vs CDC-based (Debezium, lower latency). Start with polling.

## Key Detail

Do not delete outbox rows after publishing. Mark them as published (`published_at = now()`). This preserves an audit trail and enables replay during incident recovery.
