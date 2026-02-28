# Dead Letter Handling

## What

Route messages that fail processing after retries to a **dead-letter topic (DLT)** instead of blocking the consumer or losing them.

## Why

Poison pills happen: malformed payloads, breaking schema changes, transient downstream failures that outlast your retry budget. Without a DLT, a single bad message blocks the entire partition forever (if you retry indefinitely) or gets silently dropped (if you catch and swallow the exception).

## How

Use Spring Kafka's `DefaultErrorHandler` with `DeadLetterPublishingRecoverer`. After exhausting retries, the failed message is published to `<original-topic>.DLT` with headers preserving the original topic, partition, offset, and exception.

Let exceptions propagate from the consumer to the error handler. Set up a separate DLT consumer to log and alert on failed messages.

## Key Decisions

- **Retry count:** 3 is a sane default.
- **Backoff:** Exponential (1s, 2s, 4s).
- **Non-retryable exceptions:** Deserialization errors will never succeed — skip retries.
- **DLT monitoring:** Alert when DLT consumer lag > 0. Build replay tooling.

---

## Code

### Error Handler Bean

```kotlin
@Bean
fun errorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
    val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
    val backoff = ExponentialBackOff(1000L, 2.0).apply {
        maxElapsedTime = 15_000L
    }

    return DefaultErrorHandler(recoverer, backoff).apply {
        addNotRetryableExceptions(
            SerializationException::class.java,
            MessageConversionException::class.java,
        )
    }
}
```

### Consumer

```kotlin
@KafkaListener(topics = ["orders"])
fun consume(event: OrderEvent) {
    // throws on poison pills — error handler manages retries + DLT
    processOrder(event)
}
```

### DLT Consumer

```kotlin
@KafkaListener(topics = ["orders.DLT"])
fun consumeDlt(record: ConsumerRecord<String, ByteArray>) {
    val originalTopic = headerValue(record, "kafka_dlt-original-topic")
    val exception = headerValue(record, "kafka_dlt-exception-message")
    log.error("DLT message | topic={} | exception={} | payload={}",
        originalTopic, exception, String(record.value()))
}
```
