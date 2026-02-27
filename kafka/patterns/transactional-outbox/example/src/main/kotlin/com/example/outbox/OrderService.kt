package com.example.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createOrder(customerId: String, amount: Double): Order {
        val order = orderRepository.save(Order(customerId = customerId, amount = amount))

        val payload = objectMapper.writeValueAsString(
            mapOf(
                "orderId" to order.id,
                "customerId" to order.customerId,
                "amount" to order.amount,
            )
        )

        outboxRepository.save(
            OutboxEvent(
                aggregateType = "Order",
                aggregateId = order.id!!,
                eventType = "OrderCreated",
                payload = payload,
            )
        )

        log.info("Order created: {} with outbox event", order.id)
        return order
    }
}
