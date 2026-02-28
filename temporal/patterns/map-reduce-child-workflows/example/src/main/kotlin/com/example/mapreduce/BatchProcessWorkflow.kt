package com.example.mapreduce

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface BatchProcessWorkflow {

    @WorkflowMethod
    fun processBatch(request: BatchRequest): BatchResult
}
