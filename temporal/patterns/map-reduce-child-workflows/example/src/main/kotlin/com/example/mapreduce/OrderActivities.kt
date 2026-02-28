package com.example.mapreduce

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface OrderActivities {

    @ActivityMethod
    fun validateOrder(orderId: String): OrderResult

    @ActivityMethod
    fun chargePayment(orderId: String): OrderResult

    @ActivityMethod
    fun fulfillOrder(orderId: String): OrderResult
}
