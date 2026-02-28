# Schema Evolution

## What

Evolving message schemas over time without breaking existing consumers. Producers and consumers deploy independently — a schema change in the producer must not cause deserialization failures in running consumers.

## Why

Without a schema contract, breaking changes are discovered at runtime. A renamed field, a removed required field, or a type change causes deserialization exceptions that poison your consumer.

## How

Use **Avro + Confluent Schema Registry**. The registry stores versioned schemas and enforces compatibility rules before allowing a new schema version to be registered.

Avro schema:

```json
{
  "type": "record",
  "name": "OrderEvent",
  "namespace": "com.example.schema.avro",
  "fields": [
    { "name": "orderId",    "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "amount",     "type": "double" },
    { "name": "currency",   "type": "string", "default": "USD" }
  ]
}
```

Producer:

```kotlin
val event = OrderEvent.newBuilder()
    .setOrderId("order-1")
    .setCustomerId("cust-1")
    .setAmount(99.99)
    .setCurrency("USD")
    .build()

kafkaTemplate.send("orders", event.orderId, event)
```

Consumer:

```kotlin
@KafkaListener(topics = ["orders"])
fun consume(event: OrderEvent) {
    log.info("Consumed: id={} customer={} amount={} currency={}",
        event.orderId, event.customerId, event.amount, event.currency)
}
```

### Compatibility Types

| Mode | Rule | Use When |
|------|------|----------|
| **BACKWARD** | New schema can read old data | Consumers upgrade before producers |
| **FORWARD** | Old schema can read new data | Producers upgrade before consumers |
| **FULL** | Both backward and forward | Independent deployments |
| **NONE** | No checks | Never in production |

**Recommendation:** Use `BACKWARD_TRANSITIVE`.

### Safe Changes

- Add a field **with a default value**.
- Remove a field **that had a default**.

### Unsafe Changes

- Remove a required field — old consumers fail.
- Rename a field — Avro treats this as remove + add.
- Change a field's type — deserialization fails.

## Without Avro

Embed a `version` field in the JSON payload. Use a versioned DTO hierarchy with a deserializer chain that tries the latest version first.
