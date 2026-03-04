# Bulkhead Isolation — Anti-Patterns

## 1. Shared Thread Pool for All Dependencies

**What people do:** Use the default HTTP client thread pool (or Tomcat's thread pool) for all outbound calls.

**Why it fails:** One slow dependency consumes all available threads. A payment service responding in 30s instead of 100ms holds 300x more threads per request. The shared pool saturates and every other dependency — inventory, notifications, user service — is starved. The entire service hangs.

**Instead:** Use Resilience4j bulkheads to limit concurrent calls per dependency. Each dependency gets its own concurrency budget. A slow payment service consumes at most N threads, leaving the rest available.

## 2. Bulkhead Too Large

**What people do:** Set max concurrent calls to 100 "just in case" when the dependency can only handle 20 concurrent requests.

**Why it fails:** The bulkhead does not protect the dependency — it allows 100 concurrent calls, all of which timeout because the dependency can only handle 20. You still saturate threads, just slightly fewer than without a bulkhead.

**Instead:** Size the bulkhead based on the dependency's actual capacity and expected latency. If the dependency handles 20 concurrent requests with 100ms latency, set the bulkhead to 20-25.

## 3. No Monitoring of Bulkhead Saturation

**What people do:** Configure bulkheads but do not alert when they saturate.

**Why it fails:** Bulkhead saturation means requests are being rejected. Without monitoring, you do not know traffic exceeds the dependency's capacity. The service appears healthy (no crashes) but is silently dropping requests.

**Instead:** Monitor `resilience4j.bulkhead.available.concurrent.calls`. Alert when it stays at zero for more than 30 seconds. Either increase the bulkhead, scale the dependency, or investigate the latency increase.
