package com.example.kafka.starter

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Check whether a message has already been processed.
 * Implement this interface to plug in your own deduplication strategy.
 */
interface IdempotencyChecker {
    fun isProcessed(messageId: String): Boolean
    fun markProcessed(messageId: String)
}

@Entity
@Table(name = "processed_kafka_messages")
class ProcessedKafkaMessage(
    @Id
    @Column(nullable = false, unique = true)
    val messageId: String,

    @Column(nullable = false)
    val processedAt: Instant = Instant.now(),
)

interface ProcessedKafkaMessageRepository : JpaRepository<ProcessedKafkaMessage, String>

/**
 * JPA-backed idempotency checker.
 * Requires spring-boot-starter-data-jpa on the classpath.
 */
@Component
class JpaIdempotencyChecker(
    private val repository: ProcessedKafkaMessageRepository,
) : IdempotencyChecker {

    override fun isProcessed(messageId: String): Boolean {
        return repository.existsById(messageId)
    }

    override fun markProcessed(messageId: String) {
        if (!repository.existsById(messageId)) {
            repository.save(ProcessedKafkaMessage(messageId = messageId))
        }
    }
}
