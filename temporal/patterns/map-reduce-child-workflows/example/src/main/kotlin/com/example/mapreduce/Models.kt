package com.example.mapreduce

data class BatchRequest(
    val orderIds: List<String>,
    val chunkSize: Int = 5,
)

data class BatchResult(
    val totalOrders: Int,
    val successCount: Int,
    val failureCount: Int,
    val results: List<OrderResult>,
)

data class OrderResult(
    val orderId: String,
    val success: Boolean,
    val message: String,
)
