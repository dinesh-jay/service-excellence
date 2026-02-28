# Transactional Outbox — Anti-Patterns

## 1. Dual Writes Without an Outbox

**What people do:** Save to the database, then call `kafkaTemplate.send()` in the same method — or worse, inside the `@Transactional` block hoping Spring will "roll back" the Kafka publish on failure.

**Why it fails:** Kafka publishes are not transactional with your database. If the app crashes between the DB commit and the Kafka send, the event is lost permanently. Kafka has no rollback mechanism tied to your JDBC transaction.

**Instead:** Write the event to an outbox table in the same DB transaction. Let a separate process handle publishing.

## 2. Deleting Outbox Rows Immediately After Publishing

**What people do:** `DELETE FROM outbox_events WHERE id = ?` right after the Kafka send callback succeeds.

**Why it fails:** You lose the ability to replay events during incident recovery. If a downstream consumer missed events, you have no source of truth to republish from. Deletion also makes debugging production issues harder.

**Instead:** Set `published_at = now()`. Run a scheduled archive job to move old published rows to cold storage or delete them after a retention period (e.g., 30 days).

## 3. Single Outbox Table Without Partitioning Strategy

**What people do:** All aggregates (orders, users, payments) write to a single `outbox_events` table. The polling publisher reads everything in one query.

**Why it fails:** The table becomes a write hotspot under load. The polling query locks rows across unrelated aggregates, increasing contention. A slow Kafka topic for one aggregate type blocks publishing for all others.

**Instead:** Partition by `aggregate_type` — either with separate queries per type or separate outbox tables per bounded context.
