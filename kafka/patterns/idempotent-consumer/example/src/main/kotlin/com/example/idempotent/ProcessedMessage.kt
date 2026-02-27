package com.example.idempotent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "processed_messages")
class ProcessedMessage(
    @Id
    @Column(nullable = false, unique = true)
    val messageId: String,

    @Column(nullable = false)
    val processedAt: Instant = Instant.now(),
)
