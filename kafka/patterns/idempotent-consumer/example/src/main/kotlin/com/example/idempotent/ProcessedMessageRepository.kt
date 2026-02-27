package com.example.idempotent

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedMessageRepository : JpaRepository<ProcessedMessage, String>
