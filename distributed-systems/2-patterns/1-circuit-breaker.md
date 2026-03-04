# Circuit Breaker

## What

Wrap calls to a downstream service with a circuit breaker that monitors failure rates. When failures exceed a threshold, the breaker **opens** and fails fast without making the call. After a cooldown, it moves to **half-open** and lets a few calls through to test recovery.

## Why

Without a circuit breaker, a failing downstream service causes every caller to hang until the timeout fires. Threads pile up waiting for responses that will never come. The caller's thread pool saturates, and the failure cascades to its own callers. One failing service takes down the entire call chain.

A circuit breaker stops the cascade: once the failure rate proves the dependency is down, it fails immediately. Callers get a fast error instead of a slow timeout.

## How

Use Resilience4j's `CircuitBreaker` with Spring Boot auto-configuration. Annotate the method that makes the downstream call with `@CircuitBreaker`. Configure failure rate threshold, wait duration in open state, and the number of calls in the sliding window.

Provide a fallback method that returns a degraded response (cached data, default value, or a meaningful error) instead of propagating the exception.

## Key Considerations

- **Sliding window size** — too small and the breaker flaps on a few errors. Too large and it reacts slowly. 10-20 calls is a reasonable starting point.
- **Failure rate threshold** — 50% is the default. Lower it for critical dependencies where even 20% failures indicate a problem.
- **Wait duration in open state** — how long before the breaker tries half-open. 30-60s balances fast recovery with not hammering a struggling service.
- **Record only relevant exceptions** — don't count 400 Bad Request as a failure (that is a client bug, not a dependency failure). Only count 5xx and timeouts.

---

## Code

### Dependencies (Gradle)

```kotlin
implementation("io.github.resilience4j:resilience4j-spring-boot3")
implementation("io.github.resilience4j:resilience4j-micrometer")
```

### Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      payment-service:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignore-exceptions:
          - com.myapp.exceptions.BadRequestException
```

### Service with Circuit Breaker

```kotlin
@Service
class PaymentClient(
    private val restClient: RestClient,
) {

    @CircuitBreaker(name = "payment-service", fallbackMethod = "fallback")
    fun chargePayment(request: PaymentRequest): PaymentResponse {
        return restClient.post()
            .uri("/payments")
            .body(request)
            .retrieve()
            .body(PaymentResponse::class.java)!!
    }

    private fun fallback(request: PaymentRequest, ex: Exception): PaymentResponse {
        log.warn("Payment service unavailable, circuit open: {}", ex.message)
        return PaymentResponse(
            status = PaymentStatus.PENDING,
            message = "Payment service temporarily unavailable, will retry",
        )
    }
}
```
