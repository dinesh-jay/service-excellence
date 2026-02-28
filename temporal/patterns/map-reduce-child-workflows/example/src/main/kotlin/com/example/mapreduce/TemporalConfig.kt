package com.example.mapreduce

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemporalConfig(
    @Value("\${temporal.service-address}") private val serviceAddress: String,
    @Value("\${temporal.namespace}") private val namespace: String,
    @Value("\${temporal.task-queue}") private val taskQueue: String,
) {

    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget(serviceAddress)
                .build()
        )
    }

    @Bean
    fun workflowClient(stubs: WorkflowServiceStubs): WorkflowClient {
        return WorkflowClient.newInstance(
            stubs,
            WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build()
        )
    }

    @Bean(destroyMethod = "shutdown")
    fun workerFactory(client: WorkflowClient): WorkerFactory {
        val factory = WorkerFactory.newInstance(client)
        val worker = factory.newWorker(taskQueue)

        worker.registerWorkflowImplementationTypes(
            BatchProcessWorkflowImpl::class.java,
            ProcessOrderWorkflowImpl::class.java,
        )
        worker.registerActivitiesImplementations(OrderActivitiesImpl())

        factory.start()
        return factory
    }
}
