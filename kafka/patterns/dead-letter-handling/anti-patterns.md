# Dead Letter Handling — Anti-Patterns

## 1. Infinite Retries Without a DLT

**What people do:** Configure the error handler to retry forever, assuming the issue is always transient.

**Why it fails:** A deserialization error or a null field in the payload will never succeed no matter how many times you retry. The partition is blocked — no subsequent messages are processed. Consumer lag grows, alerts fire, and the team scrambles to figure out why.

**Instead:** Set a finite retry count (3 is a good default) with exponential backoff. After exhausting retries, route to a DLT. Fix the root cause, then replay from the DLT.

## 2. Swallowing Exceptions Silently

**What people do:** Wrap the entire listener body in `try { ... } catch (e: Exception) { log.error(...) }` and let the offset commit.

**Why it fails:** The message is "processed" (offset advances), but the business logic never completed. The data is silently lost. No alert fires because there is no error from Kafka's perspective.

**Instead:** Let exceptions propagate to the error handler. The error handler manages retries and DLT routing. If you need to handle specific exceptions differently, use a custom `ErrorHandler` — do not catch-and-swallow in the listener.

## 3. DLT Without Monitoring

**What people do:** Configure DLT routing and move on. Nobody reads the DLT.

**Why it fails:** Failed messages accumulate silently. By the time someone notices, there are thousands of unprocessed events spanning weeks. The cost of replaying and reconciling grows with every ignored message.

**Instead:** Add a DLT consumer that logs failed messages with full context (original headers, exception). Set up alerts on DLT consumer lag. Build replay tooling that re-publishes corrected messages to the original topic.
