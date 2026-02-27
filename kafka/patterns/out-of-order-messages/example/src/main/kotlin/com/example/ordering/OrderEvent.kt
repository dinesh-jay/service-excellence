package com.example.ordering

data class OrderEvent(
    val orderId: String,
    val sequenceNumber: Long,
    val eventType: String,
    val payload: String,
)
