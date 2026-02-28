# Out-of-Order Messages — Anti-Patterns

## 1. Assuming Kafka Preserves Global Order

**What people do:** Treat Kafka as a FIFO queue, assuming all messages arrive in the order they were produced regardless of partition assignment.

**Why it fails:** Kafka guarantees order only within a single partition. With multiple partitions (the default for any production topic), messages for different keys land on different partitions and are consumed independently. Even within a single partition, producer retries with `max.in.flight.requests.per.connection > 1` can reorder messages.

**Instead:** Use a consistent partition key to co-locate related events on the same partition. For cross-entity ordering, use sequence numbers and consumer-side reordering.

## 2. Using Timestamps for Strict Ordering

**What people do:** Compare `event.timestamp` to determine which event happened first, processing them in timestamp order.

**Why it fails:** Clocks are not synchronized across distributed producers. Clock skew of even a few milliseconds causes events to appear in the wrong order. NTP corrections can cause clocks to jump backward. Two events produced "at the same time" on different machines have no deterministic order by timestamp.

**Instead:** Use timestamps only for last-write-wins semantics. For strict sequential processing, use a monotonically increasing sequence number assigned by a single authority.

## 3. Blocking the Consumer While Waiting for Missing Events

**What people do:** When event sequence 5 arrives but sequence 4 has not been seen, the consumer pauses or retries in a loop, holding the partition until the gap fills.

**Why it fails:** Every other message on that partition is blocked. If the missing event is delayed or never arrives, consumer lag spikes for all entities on that partition. The partition can be blocked forever.

**Instead:** Buffer the out-of-order event in a database table and acknowledge the Kafka offset immediately. A scheduled job retries buffered events periodically. If a gap is not filled within a timeout, alert and handle it.
