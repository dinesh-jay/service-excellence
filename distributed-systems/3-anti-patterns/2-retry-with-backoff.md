# Retry with Backoff — Anti-Patterns

## 1. Retrying Without Backoff

**What people do:** Retry immediately on failure, with no delay between attempts.

**Why it fails:** If the dependency is overloaded, immediate retries add more load at the worst possible time. 100 callers each retrying 3 times immediately means 300 requests hit the struggling service simultaneously. The overload gets worse, not better.

**Instead:** Use exponential backoff (1s, 2s, 4s) with jitter. Give the dependency time to recover between attempts.

## 2. Unbounded Retries

**What people do:** Set max retries to a high number (10+) or retry indefinitely.

**Why it fails:** During an outage, each caller generates 10x the normal load in retries. If the outage lasts minutes, retry traffic dominates the network. When the dependency recovers, it faces a wall of queued retries that can immediately overload it again.

**Instead:** Limit retries to 3 attempts. If 3 retries fail, the issue is not transient. Use a circuit breaker to stop retrying entirely when the failure rate proves the dependency is down.

## 3. Retrying Non-Idempotent Operations

**What people do:** Retry POST requests that create resources or trigger side effects (send email, charge payment).

**Why it fails:** The first request may have succeeded but the response was lost (network timeout). The retry creates a duplicate order, sends a second email, or charges the customer twice.

**Instead:** Only retry idempotent operations (GET, PUT with idempotency key). For non-idempotent operations, use an idempotency key pattern — the server deduplicates based on a client-provided key.
