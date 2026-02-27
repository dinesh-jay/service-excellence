# Kafka Observability

Metrics, tracing, and health checks for production Kafka workloads.

## Metrics

Use **Micrometer + KafkaClientMetrics** to expose consumer and producer JMX metrics as Micrometer gauges.

### Key Metrics to Monitor

| Metric | What It Tells You |
|--------|-------------------|
| `kafka.consumer.records.lag.max` | How far behind the consumer is. Sustained high lag = consumer is too slow or stuck. |
| `kafka.consumer.fetch.manager.records.consumed.rate` | Throughput. A sudden drop means processing is blocked. |
| `kafka.consumer.coordinator.rebalance.rate.per.hour` | Frequent rebalances indicate unstable consumers (long GC, slow processing, flapping instances). |
| `kafka.producer.record.send.rate` | Producer throughput. |
| `kafka.producer.record.error.rate` | Failed sends. Should be zero in steady state. |

### Alerting Rules

- Consumer lag > threshold for > 5 minutes — consumer cannot keep up.
- DLT topic messages > 0 — poison pills detected, investigate.
- Rebalance rate > 2/hour — consumers are unstable.
- Producer error rate > 0 — broker connectivity or serialization issues.

## Tracing

Spring Boot 4 with Micrometer Tracing + OpenTelemetry propagates trace context through Kafka automatically. Traces flow from producer to consumer without custom code.

Enable in `application.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0 # Lower in production (e.g., 0.1)
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

## Health Checks

Spring Boot Actuator's built-in Kafka health indicator checks broker connectivity. For consumer lag health, implement a custom `HealthIndicator` that queries lag via `AdminClient`.

A healthy consumer has:
- Lag below your SLA threshold (e.g., < 1000 messages).
- No unassigned partitions.
- Recent poll activity.

## Dashboards

Recommended Grafana panels:
1. Consumer lag by topic + partition (line chart)
2. Consumed/produced records per second (line chart)
3. DLT message count (stat panel with alert threshold)
4. Consumer group rebalance events (event annotations)
5. P99 processing latency (heatmap)

## See Also

- [Runnable example](./example/) — Prometheus + Grafana + OpenTelemetry setup
