package com.example.kafka.starter

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.ExponentialBackOff

@AutoConfiguration
@EnableConfigurationProperties(KafkaStarterProperties::class)
class KafkaStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun defaultErrorHandler(
        properties: KafkaStarterProperties,
        kafkaTemplate: KafkaTemplate<Any, Any>,
    ): DefaultErrorHandler {
        val backoff = ExponentialBackOff(
            properties.backoffInitialInterval,
            properties.backoffMultiplier,
        )

        val recoverer = if (properties.dltEnabled) {
            DeadLetterPublishingRecoverer(kafkaTemplate)
        } else {
            null
        }

        return DefaultErrorHandler(recoverer, backoff).apply {
            addNotRetryableExceptions(
                org.apache.kafka.common.errors.SerializationException::class.java,
                org.springframework.messaging.converter.MessageConversionException::class.java,
            )
        }
    }

    @Bean
    @ConditionalOnMissingBean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Any, Any>,
        errorHandler: DefaultErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        return ConcurrentKafkaListenerContainerFactory<Any, Any>().apply {
            this.consumerFactory = consumerFactory
            setCommonErrorHandler(errorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }
    }

    @Bean
    @ConditionalOnMissingBean(KafkaClientMetrics::class)
    @ConditionalOnBean(MeterRegistry::class)
    fun consumerMetrics(
        consumerFactory: ConsumerFactory<Any, Any>,
        meterRegistry: MeterRegistry,
    ): KafkaClientMetrics {
        val consumer: Consumer<Any, Any> = consumerFactory.createConsumer()
        return KafkaClientMetrics(consumer).also { it.bindTo(meterRegistry) }
    }
}
