package com.example.mapreduce

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BatchProcessRunner(
    private val workflowClient: WorkflowClient,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        val orderIds = (1..20).map { "ORDER-$it" }
        val request = BatchRequest(orderIds = orderIds, chunkSize = 5)

        log.info("Submitting batch of {} orders", orderIds.size)

        val workflow = workflowClient.newWorkflowStub(
            BatchProcessWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setWorkflowId("batch-demo-${System.currentTimeMillis()}")
                .setTaskQueue("batch-processing")
                .build()
        )

        val result = workflow.processBatch(request)

        log.info(
            "Batch complete: {}/{} succeeded, {}/{} failed",
            result.successCount, result.totalOrders,
            result.failureCount, result.totalOrders,
        )
    }
}
