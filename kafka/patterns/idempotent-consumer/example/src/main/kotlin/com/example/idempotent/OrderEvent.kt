package com.example.idempotent

data class OrderEvent(
    val orderId: String,
    val customerId: String,
    val amount: Double,
)
