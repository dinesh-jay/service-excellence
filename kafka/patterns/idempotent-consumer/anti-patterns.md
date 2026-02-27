# Idempotent Consumer — Anti-Patterns

## 1. Relying on Auto-Commit

**What people do:** Leave `enable.auto.commit=true` (the default) and assume messages are only delivered once.

**Why it fails:** Auto-commit runs on a timer. If the consumer crashes after the offset is committed but before processing finishes, the message is lost. If it crashes after processing but before the next auto-commit, the message is reprocessed — and your consumer is not idempotent.

**Instead:** Set `enable.auto.commit=false` and commit offsets manually after successful processing.

## 2. In-Memory Deduplication

**What people do:** Keep a `HashSet<String>` of processed message IDs in the consumer.

**Why it fails:** The set is lost on restart. It does not work across multiple consumer instances in the same group. Memory grows unbounded unless you add eviction, which reintroduces duplicates for older messages.

**Instead:** Use a persistent store (database table with unique constraint on message ID). The dedup check and business logic should share the same transaction.

## 3. Ignoring Duplicates Without Tracking

**What people do:** Assume the business logic is "naturally idempotent" (e.g., "we just update a row, so running it twice is fine") without actually tracking processed messages.

**Why it fails:** Most operations are not naturally idempotent. An "update balance by +100" executed twice doubles the effect. Even truly idempotent operations (upserts) may trigger side effects — sending emails, calling external APIs — that should not repeat.

**Instead:** Always track processed message IDs explicitly. Even if the core write is idempotent, guard side effects behind the dedup check.
