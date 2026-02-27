package com.example.ordering

import org.springframework.data.jpa.repository.JpaRepository

interface BufferedEventRepository : JpaRepository<BufferedEvent, String> {
    fun findByAggregateIdAndSequenceNumber(aggregateId: String, sequenceNumber: Long): BufferedEvent?
    fun findByAggregateIdOrderBySequenceNumberAsc(aggregateId: String): List<BufferedEvent>
}
