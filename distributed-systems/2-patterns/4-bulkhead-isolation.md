# Bulkhead Isolation

## What

Limit the number of concurrent calls to a downstream service. If one dependency is slow, only its allocated threads/permits are consumed — other dependencies remain unaffected.

## Why

Without bulkheads, all outbound calls share the same thread pool. A slow dependency (e.g., payment service responding in 30s instead of 100ms) consumes all available threads. The inventory service, notification service, and every other dependency are now starved of threads. One slow service takes down all outbound communication.

Bulkheads isolate the blast radius: the payment service can consume at most N threads, leaving the rest available for healthy dependencies.

## How

Use Resilience4j's `Bulkhead` (semaphore-based) or `ThreadPoolBulkhead` (thread-pool-based). The semaphore bulkhead limits concurrent calls on the caller's thread. The thread pool bulkhead runs calls on a dedicated thread pool with a bounded queue.

Semaphore bulkhead is simpler and lower overhead. Thread pool bulkhead provides full isolation but adds thread-switching cost.

## Key Considerations

- **Semaphore vs thread pool** — use semaphore for most cases. Use thread pool when you need true isolation (e.g., a dependency that blocks the calling thread).
- **Max concurrent calls** — size based on the dependency's capacity and expected latency. Too low = unnecessary rejections. Too high = no protection.
- **Max wait duration** — how long to wait for a permit. Zero fails fast. Set to a small value (100-500ms) if callers can tolerate brief queuing.
- **Combine with other patterns** — typical ordering: `CircuitBreaker(Bulkhead(RateLimiter(Retry(call))))`.

---

## Code

### Configuration (Semaphore Bulkhead)

```yaml
resilience4j:
  bulkhead:
    instances:
      payment-service:
        max-concurrent-calls: 10
        max-wait-duration: 100ms
      inventory-service:
        max-concurrent-calls: 20
        max-wait-duration: 0ms         # fail fast
```

### Service with Bulkhead

```kotlin
@Service
class PaymentClient(
    private val restClient: RestClient,
) {

    @CircuitBreaker(name = "payment-service", fallbackMethod = "fallback")
    @Bulkhead(name = "payment-service")
    @Retry(name = "payment-service")
    fun chargePayment(request: PaymentRequest): PaymentResponse {
        return restClient.post()
            .uri("/payments")
            .body(request)
            .retrieve()
            .body(PaymentResponse::class.java)!!
    }

    private fun fallback(request: PaymentRequest, ex: Exception): PaymentResponse {
        log.warn("Payment service bulkhead full or failing: {}", ex.message)
        return PaymentResponse(status = PaymentStatus.PENDING)
    }
}
```

### Thread Pool Bulkhead (When Full Isolation Needed)

```yaml
resilience4j:
  thread-pool-bulkhead:
    instances:
      legacy-service:
        max-thread-pool-size: 5
        core-thread-pool-size: 3
        queue-capacity: 10
        keep-alive-duration: 100ms
```
