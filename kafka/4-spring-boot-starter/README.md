# Kafka Spring Boot Starter

An opinionated auto-configuration for production Kafka usage with Spring Boot 4.

## What It Provides

- **Auto-commit disabled, manual ack mode** — you control when offsets are committed.
- **Dead-letter topic routing** — failed messages go to `<topic>.DLT` after retries.
- **Exponential backoff retries** — 3 attempts with 1s/2s/4s backoff by default.
- **JSON serde with type headers** — `JsonSerializer`/`JsonDeserializer` with trusted packages.
- **Micrometer metrics** — `KafkaClientMetrics` auto-registered.
- **Idempotency support** — interface and JPA-based implementation for dedup checking.

## Opinionated Defaults

| Setting | Value | Why |
|---------|-------|-----|
| `enable.auto.commit` | `false` | Prevents offset commit before processing completes |
| `ack-mode` | `MANUAL` | Explicit offset control |
| `isolation.level` | `read_committed` | Ignores uncommitted transactional messages |
| Retry count | 3 | Enough for transient failures, not enough to block on poison pills |
| Backoff | Exponential 1s/2s/4s | Gives downstream time to recover |
| DLT | Enabled | Failed messages are preserved, not dropped |

All beans are `@ConditionalOnMissingBean`. Define your own to replace defaults.

---

## Code

### Configuration Properties

```kotlin
@ConfigurationProperties(prefix = "app.kafka")
data class KafkaStarterProperties(
    val retryAttempts: Int = 3,
    val backoffInitialInterval: Long = 1000L,
    val backoffMultiplier: Double = 2.0,
    val dltEnabled: Boolean = true,
    val trustedPackages: List<String> = listOf("*"),
)
```

### Auto-Configuration

```kotlin
@AutoConfiguration
@EnableConfigurationProperties(KafkaStarterProperties::class)
class KafkaStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun defaultErrorHandler(
        properties: KafkaStarterProperties,
        kafkaTemplate: KafkaTemplate<Any, Any>,
    ): DefaultErrorHandler {
        val backoff = ExponentialBackOff(properties.backoffInitialInterval, properties.backoffMultiplier)
        val recoverer = if (properties.dltEnabled) DeadLetterPublishingRecoverer(kafkaTemplate) else null
        return DefaultErrorHandler(recoverer, backoff)
    }

    @Bean
    @ConditionalOnMissingBean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Any, Any>,
        errorHandler: DefaultErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        return ConcurrentKafkaListenerContainerFactory<Any, Any>().apply {
            this.consumerFactory = consumerFactory
            setCommonErrorHandler(errorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }
    }
}
```

### Idempotency Support

```kotlin
interface IdempotencyChecker {
    fun isProcessed(messageId: String): Boolean
    fun markProcessed(messageId: String)
}

// JPA-backed implementation provided out of the box
class JpaIdempotencyChecker(
    private val repository: ProcessedKafkaMessageRepository,
) : IdempotencyChecker { ... }
```

### Override Defaults

```yaml
app:
  kafka:
    retry-attempts: 5
    backoff-initial-interval: 2000
    backoff-multiplier: 3.0
    dlt-enabled: false
    trusted-packages:
      - com.myapp.events
```
