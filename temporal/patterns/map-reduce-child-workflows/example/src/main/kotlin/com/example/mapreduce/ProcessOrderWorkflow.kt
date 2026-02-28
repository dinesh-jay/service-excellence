package com.example.mapreduce

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface ProcessOrderWorkflow {

    @WorkflowMethod
    fun processOrders(orderIds: List<String>): List<OrderResult>
}
