package com.example.ordering

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OutOfOrderMessagesApplication

fun main(args: Array<String>) {
    runApplication<OutOfOrderMessagesApplication>(*args)
}
