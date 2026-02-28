package com.example.mapreduce

import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class ProcessOrderWorkflowImpl : ProcessOrderWorkflow {

    private val log = Workflow.getLogger(ProcessOrderWorkflowImpl::class.java)

    private val activities = Workflow.newActivityStub(
        OrderActivities::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )

    override fun processOrders(orderIds: List<String>): List<OrderResult> {
        log.info("Processing chunk of {} orders", orderIds.size)

        return orderIds.map { orderId ->
            try {
                activities.validateOrder(orderId)
                activities.chargePayment(orderId)
                activities.fulfillOrder(orderId)
            } catch (e: Exception) {
                log.error("Failed to process order {}: {}", orderId, e.message)
                OrderResult(orderId = orderId, success = false, message = e.message ?: "Unknown error")
            }
        }
    }
}
