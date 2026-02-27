package com.example.observability

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaObservabilityApplication

fun main(args: Array<String>) {
    runApplication<KafkaObservabilityApplication>(*args)
}
