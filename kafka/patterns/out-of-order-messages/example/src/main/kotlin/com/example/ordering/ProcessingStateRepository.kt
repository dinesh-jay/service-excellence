package com.example.ordering

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessingStateRepository : JpaRepository<ProcessingState, String>
