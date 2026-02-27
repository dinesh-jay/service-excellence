package com.example.idempotent

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderEventConsumer(
    private val processedMessageRepository: ProcessedMessageRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["orders"])
    @Transactional
    fun consume(record: ConsumerRecord<String, OrderEvent>, ack: Acknowledgment) {
        val messageId = extractMessageId(record)

        if (processedMessageRepository.existsById(messageId)) {
            log.info("Duplicate message detected, skipping: {}", messageId)
            ack.acknowledge()
            return
        }

        // --- Business logic ---
        log.info("Processing order: {} for customer: {}", record.value().orderId, record.value().customerId)

        // Mark as processed in the same transaction
        processedMessageRepository.save(ProcessedMessage(messageId = messageId))

        ack.acknowledge()
    }

    private fun extractMessageId(record: ConsumerRecord<String, OrderEvent>): String {
        val header = record.headers().lastHeader("message_id")
        if (header != null) {
            return String(header.value())
        }
        // Fallback: use topic-partition-offset as a natural dedup key
        return "${record.topic()}-${record.partition()}-${record.offset()}"
    }
}
