# Out-of-Order Messages

## What

Handling messages that arrive in a different sequence than they were logically produced. Kafka guarantees ordering only within a partition — across partitions and during retries or rebalances, order is not guaranteed.

## Why

Events for the same entity (e.g., OrderCreated, OrderUpdated, OrderShipped) may arrive out of sequence. Processing OrderShipped before OrderCreated corrupts state.

## How

Three strategies, progressively more complex:

### 1. Partition Key Strategy

Route events for the same aggregate to the same partition using a consistent key (e.g., `orderId`). Simplest approach — handles most cases.

### 2. Sequence-Number Reordering

Each event carries a monotonic sequence number per aggregate. The consumer tracks `lastProcessedSequence` per entity. If the next expected sequence arrives, process it. If a future event arrives, buffer it. If a stale/duplicate arrives, skip it. A scheduled job retries buffered events when gaps fill.

### 3. Timestamp-Based Last-Write-Wins

Compare event timestamp with stored entity's `lastUpdatedAt`. Apply only if newer. Suitable for CDC-style idempotent updates, not for strict sequential processing (clock skew makes this unreliable).

**Recommendation:** Start with partition keys. Add sequence-number reordering only when you have multiple producers or cross-partition ordering requirements.

---

## Code

### Consumer with Sequence-Number Reordering

```kotlin
@KafkaListener(topics = ["order-events"])
@Transactional
fun consume(record: ConsumerRecord<String, OrderEvent>, ack: Acknowledgment) {
    val event = record.value()
    val state = processingStateRepository.findById(event.orderId)
        .orElse(ProcessingState(aggregateId = event.orderId))

    val expected = state.lastProcessedSequence + 1

    when {
        event.sequenceNumber == expected -> {
            processEvent(event)
            state.lastProcessedSequence = event.sequenceNumber
            processingStateRepository.save(state)
            drainBuffer(event.orderId, state)  // process any buffered events now contiguous
        }
        event.sequenceNumber > expected -> {
            // future event — buffer it, ack the offset
            bufferedEventRepository.save(BufferedEvent(
                aggregateId = event.orderId,
                sequenceNumber = event.sequenceNumber,
                eventType = event.eventType,
                payload = event.payload,
            ))
        }
        else -> {
            // stale/duplicate — skip
            log.info("Stale event for {}: last={}, got={}", event.orderId,
                state.lastProcessedSequence, event.sequenceNumber)
        }
    }

    ack.acknowledge()
}
```

### Buffer Processor

```kotlin
@Scheduled(fixedDelay = 5000)
@Transactional
fun processBufferedEvents() {
    for (state in processingStateRepository.findAll()) {
        var next = state.lastProcessedSequence + 1
        while (true) {
            val buffered = bufferedEventRepository
                .findByAggregateIdAndSequenceNumber(state.aggregateId, next) ?: break
            processEvent(buffered.toOrderEvent())
            state.lastProcessedSequence = next
            bufferedEventRepository.delete(buffered)
            next++
        }
        processingStateRepository.save(state)
    }
}
```
