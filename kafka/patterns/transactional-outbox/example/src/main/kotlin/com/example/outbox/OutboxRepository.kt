package com.example.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxRepository : JpaRepository<OutboxEvent, String> {
    fun findByPublishedAtIsNull(): List<OutboxEvent>
}
