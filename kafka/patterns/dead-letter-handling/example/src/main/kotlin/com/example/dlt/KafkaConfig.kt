package com.example.dlt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.util.backoff.ExponentialBackOff

@Configuration
class KafkaConfig {

    @Bean
    fun errorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate)

        val backoff = ExponentialBackOff(1000L, 2.0).apply {
            maxElapsedTime = 15_000L // stop retrying after 15 seconds
        }

        return DefaultErrorHandler(recoverer, backoff).apply {
            // Do not retry deserialization errors — they will never succeed
            addNotRetryableExceptions(
                org.apache.kafka.common.errors.SerializationException::class.java,
                org.springframework.messaging.converter.MessageConversionException::class.java,
            )
        }
    }
}
