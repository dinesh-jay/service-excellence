# Kafka Spring Boot Starter

An opinionated auto-configuration for production Kafka usage with Spring Boot 4.

## What It Provides

- **Auto-commit disabled, manual ack mode** — you control when offsets are committed.
- **Dead-letter topic routing** — failed messages go to `<topic>.DLT` after retries, not into the void.
- **Exponential backoff retries** — 3 attempts with 1s/2s/4s backoff by default.
- **JSON serde with type headers** — `JsonSerializer`/`JsonDeserializer` with trusted packages configured.
- **Micrometer metrics** — `KafkaClientMetrics` auto-registered for consumer and producer JMX metrics.
- **Idempotency support** — interface and JPA-based implementation for dedup checking.
- **Read-committed isolation** — consumers only see committed transactional messages.

## Usage

Add the starter as a dependency:

```kotlin
dependencies {
    implementation(project(":kafka-spring-boot-starter"))
}
```

All defaults are applied via auto-configuration. No additional `@Bean` definitions needed.

## Configuration

Override defaults via `application.yml`:

```yaml
app:
  kafka:
    retry-attempts: 5              # default: 3
    backoff-initial-interval: 2000  # default: 1000ms
    backoff-multiplier: 3.0         # default: 2.0
    dlt-enabled: false              # default: true
    trusted-packages:               # default: ["*"]
      - com.myapp.events
      - com.myapp.commands
```

## Opinionated Defaults

| Setting | Value | Why |
|---------|-------|-----|
| `enable.auto.commit` | `false` | Prevents offset commit before processing completes |
| `ack-mode` | `MANUAL` | Explicit offset control |
| `isolation.level` | `read_committed` | Ignores uncommitted transactional messages |
| Retry count | 3 | Enough for transient failures, not enough to block on poison pills |
| Backoff | Exponential 1s/2s/4s | Gives downstream time to recover |
| DLT | Enabled | Failed messages are preserved, not dropped |

## Overriding

All beans are `@ConditionalOnMissingBean`. Define your own `DefaultErrorHandler`, `ConcurrentKafkaListenerContainerFactory`, or `KafkaClientMetrics` to replace the starter's defaults.
