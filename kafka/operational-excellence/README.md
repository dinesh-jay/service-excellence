# Kafka Operational Excellence

Metrics, tracing, health checks, and alerting for production Kafka workloads.

## Metrics

Use **Micrometer + KafkaClientMetrics** to expose consumer and producer JMX metrics as Micrometer gauges.

```kotlin
@Bean
fun kafkaConsumerMetrics(
    consumerFactory: ConsumerFactory<String, String>,
    meterRegistry: MeterRegistry,
): KafkaClientMetrics {
    val consumer = consumerFactory.createConsumer()
    return KafkaClientMetrics(consumer).also { it.bindTo(meterRegistry) }
}
```

### Key Metrics to Monitor

| Metric | What It Tells You |
|--------|-------------------|
| `kafka.consumer.records.lag.max` | How far behind the consumer is. Sustained high lag = too slow or stuck. |
| `kafka.consumer.fetch.manager.records.consumed.rate` | Throughput. A sudden drop means processing is blocked. |
| `kafka.consumer.coordinator.rebalance.rate.per.hour` | Frequent rebalances indicate unstable consumers. |
| `kafka.producer.record.send.rate` | Producer throughput. |
| `kafka.producer.record.error.rate` | Failed sends. Should be zero in steady state. |

### Alerting Rules

- Consumer lag > threshold for > 5 minutes — consumer cannot keep up.
- DLT topic messages > 0 — poison pills detected.
- Rebalance rate > 2/hour — consumers are unstable.
- Producer error rate > 0 — broker or serialization issues.

## Tracing

Spring Boot 4 with Micrometer Tracing + OpenTelemetry propagates trace context through Kafka automatically.

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # lower in production (e.g., 0.1)
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

## Health Checks

Spring Boot Actuator's built-in Kafka health indicator checks broker connectivity. For consumer lag health, implement a custom `HealthIndicator`:

```kotlin
@Component
class ConsumerLagHealthIndicator(
    private val kafkaAdmin: KafkaAdmin,
) : HealthIndicator {

    override fun health(): Health {
        val adminClient = AdminClient.create(kafkaAdmin.configurationProperties)
        adminClient.use { client ->
            val offsets = client.listConsumerGroupOffsets(groupId)
                .partitionsToOffsetAndMetadata().get()

            val endOffsets = client.listOffsets(
                offsets.keys.associateWith { OffsetSpec.latest() }
            ).all().get()

            val maxLag = offsets.maxOf { (tp, committed) ->
                (endOffsets[tp]?.offset() ?: 0L) - committed.offset()
            }

            return if (maxLag > lagThreshold) Health.down() else Health.up()
                .withDetail("maxLag", maxLag)
                .build()
        }
    }
}
```

## Dashboards

Recommended Grafana panels:
1. Consumer lag by topic + partition (line chart)
2. Consumed/produced records per second (line chart)
3. DLT message count (stat panel with alert threshold)
4. Consumer group rebalance events (event annotations)
5. P99 processing latency (heatmap)
