package com.example.mapreduce

import org.slf4j.LoggerFactory

class OrderActivitiesImpl : OrderActivities {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun validateOrder(orderId: String): OrderResult {
        log.info("Validating order {}", orderId)
        Thread.sleep(500)
        return OrderResult(orderId = orderId, success = true, message = "Validated")
    }

    override fun chargePayment(orderId: String): OrderResult {
        log.info("Charging payment for order {}", orderId)
        Thread.sleep(300)
        return OrderResult(orderId = orderId, success = true, message = "Payment charged")
    }

    override fun fulfillOrder(orderId: String): OrderResult {
        log.info("Fulfilling order {}", orderId)
        Thread.sleep(200)
        return OrderResult(orderId = orderId, success = true, message = "Fulfilled")
    }
}
