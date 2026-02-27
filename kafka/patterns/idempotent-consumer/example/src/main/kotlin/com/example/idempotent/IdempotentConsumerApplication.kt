package com.example.idempotent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IdempotentConsumerApplication

fun main(args: Array<String>) {
    runApplication<IdempotentConsumerApplication>(*args)
}
