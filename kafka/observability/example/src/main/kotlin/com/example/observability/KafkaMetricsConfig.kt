package com.example.observability

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaMetricsConfig {

    @Bean
    fun kafkaConsumerMetrics(
        consumerFactory: ConsumerFactory<String, String>,
        meterRegistry: MeterRegistry,
    ): KafkaClientMetrics {
        val consumer = consumerFactory.createConsumer()
        return KafkaClientMetrics(consumer).also { it.bindTo(meterRegistry) }
    }

    @Bean
    fun kafkaProducerMetrics(
        producerFactory: ProducerFactory<String, String>,
        meterRegistry: MeterRegistry,
    ): KafkaClientMetrics {
        val producer = producerFactory.createProducer()
        return KafkaClientMetrics(producer).also { it.bindTo(meterRegistry) }
    }
}
