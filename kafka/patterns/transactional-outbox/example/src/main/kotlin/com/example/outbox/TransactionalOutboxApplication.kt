package com.example.outbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TransactionalOutboxApplication

fun main(args: Array<String>) {
    runApplication<TransactionalOutboxApplication>(*args)
}
