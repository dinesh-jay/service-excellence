package com.example.outbox

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun publishPendingEvents() {
        val events = outboxRepository.findByPublishedAtIsNull()
        if (events.isEmpty()) return

        log.info("Publishing {} pending outbox events", events.size)

        for (event in events) {
            val topic = "${event.aggregateType.lowercase()}-events"

            kafkaTemplate.send(topic, event.aggregateId, event.payload)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        log.error("Failed to publish outbox event {}: {}", event.id, ex.message)
                    } else {
                        log.info(
                            "Published outbox event {} to {}@{}",
                            event.id, result.recordMetadata.topic(), result.recordMetadata.offset()
                        )
                    }
                }

            event.publishedAt = Instant.now()
            outboxRepository.save(event)
        }
    }
}
