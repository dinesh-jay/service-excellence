package com.example.schema

import com.example.schema.avro.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class CreateOrderRequest(
    val orderId: String,
    val customerId: String,
    val amount: Double,
    val currency: String = "USD",
)

@RestController
class OrderEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, OrderEvent>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/orders")
    fun produce(@RequestBody request: CreateOrderRequest) {
        val event = OrderEvent.newBuilder()
            .setOrderId(request.orderId)
            .setCustomerId(request.customerId)
            .setAmount(request.amount)
            .setCurrency(request.currency)
            .build()

        kafkaTemplate.send("orders", request.orderId, event)
        log.info("Produced order event: {}", request.orderId)
    }
}
