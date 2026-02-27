package com.example.ordering

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "processing_state")
class ProcessingState(
    @Id
    @Column(nullable = false)
    val aggregateId: String,

    @Column(nullable = false)
    var lastProcessedSequence: Long = 0,
)
