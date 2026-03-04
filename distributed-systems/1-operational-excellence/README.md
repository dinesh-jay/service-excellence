# Distributed Systems Operational Excellence

Distributed tracing, cross-service observability, and resilience metrics for production distributed services.

## Distributed Tracing

Use OpenTelemetry with Micrometer Tracing to propagate trace context across service boundaries. Every HTTP request generates a trace ID that flows through all downstream calls — HTTP, Kafka, gRPC.

### Key Metrics to Monitor

| Metric | What It Tells You |
|--------|-------------------|
| `resilience4j.circuitbreaker.state` | Circuit state per backend: 0=closed, 1=open, 2=half-open. Open = dependency is down. |
| `resilience4j.circuitbreaker.failure.rate` | Failure percentage. Approaches the threshold before the breaker opens. |
| `resilience4j.retry.calls` | Retry attempts by outcome (success, retry, failure). High retries = flaky dependency. |
| `resilience4j.bulkhead.available.concurrent.calls` | Available capacity. Zero = all permits used, requests are being rejected. |
| `resilience4j.ratelimiter.available.permissions` | Remaining rate limit permits. Zero = throttling active. |
| `http.client.requests` (by target service) | Latency and error rate per downstream dependency. |

### Alerting Rules

- Circuit breaker OPEN for > 1 minute — dependency is down, investigate.
- Retry rate > 20% of total calls — dependency is flaky, may need attention.
- Bulkhead saturation (available = 0) for > 30s — increase permits or scale dependency.
- Rate limiter rejections > 0 — traffic exceeds budget, check for load spikes.
- Cross-service P99 latency > SLO — degraded performance, check tracing.

## Health Checks

Expose the state of circuit breakers and bulkheads via Actuator health indicators. A circuit breaker in OPEN state should make the readiness probe return DOWN for that specific dependency (not the entire service).

## Dashboards

Recommended Grafana panels:
1. Circuit breaker state by backend (state timeline)
2. Request rate and error rate by downstream service (line chart)
3. Retry attempts vs successes (stacked bar)
4. Bulkhead utilization (gauge)
5. Cross-service trace latency distribution (heatmap)

---

## Code

### OpenTelemetry Tracing Configuration

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # lower in production (e.g., 0.1)
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

### Resilience4j Metrics Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
    metrics:
      enabled: true
  retry:
    metrics:
      enabled: true
  bulkhead:
    metrics:
      enabled: true
```

### Circuit Breaker Health Indicator

```kotlin
@Component
class CircuitBreakerHealthIndicator(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) : HealthIndicator {

    override fun health(): Health {
        val openBreakers = circuitBreakerRegistry.allCircuitBreakers
            .filter { it.state == CircuitBreaker.State.OPEN }
            .map { it.name }

        return if (openBreakers.isEmpty()) {
            Health.up().build()
        } else {
            Health.down()
                .withDetail("openCircuitBreakers", openBreakers)
                .build()
        }
    }
}
```
