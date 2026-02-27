package com.example.ordering

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BufferProcessor(
    private val processingStateRepository: ProcessingStateRepository,
    private val bufferedEventRepository: BufferedEventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun processBufferedEvents() {
        val allStates = processingStateRepository.findAll()
        if (allStates.isEmpty()) return

        for (state in allStates) {
            val aggregateId = state.aggregateId
            var nextSequence = state.lastProcessedSequence + 1

            while (true) {
                val buffered = bufferedEventRepository
                    .findByAggregateIdAndSequenceNumber(aggregateId, nextSequence) ?: break

                log.info(
                    "Buffer processor: processing buffered event for {} seq={}",
                    aggregateId, nextSequence,
                )

                log.info(
                    "Processing event: orderId={}, seq={}, type={}, payload={}",
                    aggregateId, buffered.sequenceNumber, buffered.eventType, buffered.payload,
                )

                state.lastProcessedSequence = nextSequence
                processingStateRepository.save(state)
                bufferedEventRepository.delete(buffered)

                nextSequence++
            }
        }
    }
}
