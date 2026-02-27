package com.example.dlt

data class OrderEvent(
    val orderId: String,
    val customerId: String,
    val amount: Double,
)
