package com.example.schema

import com.example.schema.avro.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["orders"])
    fun consume(event: OrderEvent) {
        log.info(
            "Consumed order: id={} customer={} amount={} currency={}",
            event.orderId,
            event.customerId,
            event.amount,
            event.currency,
        )
    }
}
