package com.example.dlt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeadLetterHandlingApplication

fun main(args: Array<String>) {
    runApplication<DeadLetterHandlingApplication>(*args)
}
