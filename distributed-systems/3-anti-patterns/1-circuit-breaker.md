# Circuit Breaker — Anti-Patterns

## 1. No Circuit Breaker at All

**What people do:** Call downstream services directly with only a timeout. Hope for the best.

**Why it fails:** When the dependency is down, every call hangs until the timeout fires. With a 10s timeout and 100 concurrent requests, you have 100 threads blocked for 10s each. Your thread pool saturates and the failure cascades to your callers. One failing dependency takes down the entire service.

**Instead:** Wrap every outbound service call with a circuit breaker. When the failure rate exceeds the threshold, fail immediately instead of waiting for the timeout.

## 2. Circuit Breaking on Client Errors

**What people do:** Configure the circuit breaker to record all exceptions, including 400 Bad Request and 404 Not Found.

**Why it fails:** Client errors (4xx) are not dependency failures — they are bugs in the caller's request. A batch of malformed requests opens the circuit breaker, and now valid requests are also rejected. The dependency is healthy but the breaker says it is down.

**Instead:** Only record server errors (5xx) and timeouts. Use `ignore-exceptions` or `record-exceptions` to exclude client errors.

## 3. No Fallback

**What people do:** Let the circuit breaker throw `CallNotPermittedException` when open, which propagates as a 500 to the caller.

**Why it fails:** The whole point of a circuit breaker is graceful degradation. Returning a 500 is not graceful — it is just a faster failure. The caller still sees an error and may cascade it further.

**Instead:** Provide a fallback method that returns a degraded response: cached data, a default value, or a meaningful error with a retry-after hint.
