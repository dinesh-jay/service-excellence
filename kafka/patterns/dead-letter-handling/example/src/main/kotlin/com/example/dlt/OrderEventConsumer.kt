package com.example.dlt

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["orders"])
    fun consume(event: OrderEvent) {
        log.info("Processing order: {} for customer: {}", event.orderId, event.customerId)

        // Simulate business logic that might fail
        if (event.amount <= 0) {
            throw IllegalArgumentException("Order amount must be positive: ${event.amount}")
        }

        log.info("Order {} processed successfully", event.orderId)
    }
}
