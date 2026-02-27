package com.example.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, String>
