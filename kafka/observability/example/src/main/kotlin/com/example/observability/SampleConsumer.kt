package com.example.observability

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SampleConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["demo-topic"])
    fun consume(message: String) {
        log.info("Consumed message: {}", message)
    }
}
