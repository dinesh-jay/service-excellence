package com.example.kafka.starter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka")
data class KafkaStarterProperties(
    val retryAttempts: Int = 3,
    val backoffInitialInterval: Long = 1000L,
    val backoffMultiplier: Double = 2.0,
    val dltEnabled: Boolean = true,
    val trustedPackages: List<String> = listOf("*"),
)
