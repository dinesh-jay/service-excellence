package com.example.observability

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/send")
    fun send(@RequestParam message: String): String {
        kafkaTemplate.send("demo-topic", message)
        log.info("Produced message: {}", message)
        return "Sent: $message"
    }
}
