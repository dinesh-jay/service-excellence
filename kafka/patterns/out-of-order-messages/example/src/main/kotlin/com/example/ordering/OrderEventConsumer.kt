package com.example.ordering

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderEventConsumer(
    private val processingStateRepository: ProcessingStateRepository,
    private val bufferedEventRepository: BufferedEventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["order-events"])
    @Transactional
    fun consume(record: ConsumerRecord<String, OrderEvent>, ack: Acknowledgment) {
        val event = record.value()
        val aggregateId = event.orderId
        val sequence = event.sequenceNumber

        val state = processingStateRepository.findById(aggregateId)
            .orElse(ProcessingState(aggregateId = aggregateId))

        val expected = state.lastProcessedSequence + 1

        when {
            sequence == expected -> {
                processEvent(event)
                state.lastProcessedSequence = sequence
                processingStateRepository.save(state)
                drainBuffer(aggregateId, state)
            }
            sequence > expected -> {
                log.warn(
                    "Out-of-order event for {}: expected={}, got={}. Buffering.",
                    aggregateId, expected, sequence,
                )
                bufferedEventRepository.save(
                    BufferedEvent(
                        aggregateId = aggregateId,
                        sequenceNumber = sequence,
                        eventType = event.eventType,
                        payload = event.payload,
                    )
                )
            }
            else -> {
                log.info(
                    "Stale event for {}: lastProcessed={}, got={}. Skipping.",
                    aggregateId, state.lastProcessedSequence, sequence,
                )
            }
        }

        ack.acknowledge()
    }

    private fun drainBuffer(aggregateId: String, state: ProcessingState) {
        while (true) {
            val next = state.lastProcessedSequence + 1
            val buffered = bufferedEventRepository
                .findByAggregateIdAndSequenceNumber(aggregateId, next) ?: break

            processEvent(
                OrderEvent(
                    orderId = aggregateId,
                    sequenceNumber = buffered.sequenceNumber,
                    eventType = buffered.eventType,
                    payload = buffered.payload,
                )
            )

            state.lastProcessedSequence = next
            processingStateRepository.save(state)
            bufferedEventRepository.delete(buffered)
        }
    }

    private fun processEvent(event: OrderEvent) {
        log.info(
            "Processing event: orderId={}, seq={}, type={}, payload={}",
            event.orderId, event.sequenceNumber, event.eventType, event.payload,
        )
        // --- Business logic goes here ---
    }
}
