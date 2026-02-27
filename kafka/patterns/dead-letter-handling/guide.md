# Dead Letter Handling

## What

Route messages that fail processing after retries to a **dead-letter topic (DLT)** instead of blocking the consumer or losing them.

## Why

Poison pills happen: malformed payloads, breaking schema changes, transient downstream failures that outlast your retry budget. Without a DLT, a single bad message blocks the entire partition forever (if you retry indefinitely) or gets silently dropped (if you catch and swallow the exception).

## How

Use Spring Kafka's `DefaultErrorHandler` with `DeadLetterPublishingRecoverer`. After exhausting retries, the failed message is published to `<original-topic>.DLT` with headers preserving the original topic, partition, offset, and exception.

```kotlin
@Bean
fun errorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
    val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)
    val backoff = ExponentialBackOff(1000L, 2.0).apply { maxElapsedTime = 15_000L }
    return DefaultErrorHandler(recoverer, backoff)
}
```

## Key Decisions

- **Retry count:** 3 is a sane default. More retries rarely help — if it failed 3 times, it's not a transient blip.
- **Backoff strategy:** Exponential (1s, 2s, 4s). Gives downstream services time to recover without overwhelming them.
- **DLT headers:** `DefaultErrorHandler` automatically adds `kafka_dlt-original-topic`, `kafka_dlt-original-partition`, `kafka_dlt-original-offset`, and `kafka_dlt-exception-message`. Use these for debugging.

## DLT Consumers

Do not let DLT messages rot. Build operational tooling:

1. **Monitor** — alert when the DLT topic receives messages (consumer lag on DLT > 0).
2. **Inspect** — a DLT consumer that logs the failed message with original context.
3. **Replay** — tooling to re-publish fixed messages back to the original topic after the root cause is resolved.

## See Also

- [Runnable example](./example/) — Spring Boot 4 with DLT routing
- [Anti-patterns](./anti-patterns.md) — common mistakes
