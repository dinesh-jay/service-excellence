package com.example.dlt

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DltConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["orders.DLT"],
        properties = [
            "value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer",
        ],
    )
    fun consume(record: ConsumerRecord<String, ByteArray>) {
        val originalTopic = headerValue(record, "kafka_dlt-original-topic")
        val originalPartition = headerValue(record, "kafka_dlt-original-partition")
        val originalOffset = headerValue(record, "kafka_dlt-original-offset")
        val exception = headerValue(record, "kafka_dlt-exception-message")

        log.error(
            "DLT message received | original-topic={} partition={} offset={} | exception={} | payload={}",
            originalTopic,
            originalPartition,
            originalOffset,
            exception,
            String(record.value()),
        )
    }

    private fun headerValue(record: ConsumerRecord<String, ByteArray>, key: String): String? {
        return record.headers().lastHeader(key)?.let { String(it.value()) }
    }
}
