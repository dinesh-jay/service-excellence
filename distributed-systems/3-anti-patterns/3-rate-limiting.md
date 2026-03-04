# Rate Limiting — Anti-Patterns

## 1. No Rate Limiting

**What people do:** Forward all incoming traffic to downstream dependencies without any rate control.

**Why it fails:** A traffic spike (marketing campaign, bot scraping, retry storm) propagates through the entire system. The downstream dependency gets overwhelmed, starts returning errors, and those errors trigger retries — amplifying the overload. This is the thundering herd problem.

**Instead:** Rate-limit outbound calls to match the dependency's capacity. Rate-limit inbound traffic to match your own capacity. Shed excess load early rather than letting it cascade.

## 2. Per-Instance Limits Without Coordination

**What people do:** Set a rate limit of 100/s per instance, deploy 10 instances, and the downstream sees 1000/s when the provider's limit is 500/s.

**Why it fails:** Per-instance rate limiting does not account for the total number of instances. Auto-scaling makes it worse — adding instances increases the aggregate rate.

**Instead:** Divide the global limit by the expected instance count with a safety margin. For precise global limiting, use a centralized rate limiter (Redis-backed or API gateway).

## 3. No Backpressure Signal

**What people do:** Rate-limit and silently drop or reject requests without informing the caller.

**Why it fails:** The caller does not know it is being rate-limited. It may retry the rejected request, increasing load. Monitoring does not capture the rejected traffic, hiding the real demand signal.

**Instead:** Return HTTP 429 with a `Retry-After` header. Log and metric every rejection. Callers can back off or queue. Operators can see the true traffic demand.
