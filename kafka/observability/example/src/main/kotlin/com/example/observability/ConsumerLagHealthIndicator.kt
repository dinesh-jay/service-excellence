package com.example.observability

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Component

@Component
class ConsumerLagHealthIndicator(
    private val kafkaAdmin: KafkaAdmin,
) : HealthIndicator {

    private val groupId = "observability-demo-group"
    private val lagThreshold = 1000L

    override fun health(): Health {
        return try {
            val adminClient = AdminClient.create(kafkaAdmin.configurationProperties)
            adminClient.use { client ->
                val offsets: Map<TopicPartition, OffsetAndMetadata> =
                    client.listConsumerGroupOffsets(groupId)
                        .partitionsToOffsetAndMetadata()
                        .get()

                if (offsets.isEmpty()) {
                    return Health.unknown().withDetail("reason", "No committed offsets found").build()
                }

                val endOffsets = client.listOffsets(
                    offsets.keys.associateWith {
                        org.apache.kafka.clients.admin.OffsetSpec.latest()
                    }
                ).all().get()

                val lags = offsets.map { (tp, committed) ->
                    val endOffset = endOffsets[tp]?.offset() ?: 0L
                    val lag = endOffset - committed.offset()
                    tp.toString() to lag
                }.toMap()

                val maxLag = lags.values.maxOrNull() ?: 0L

                val builder = if (maxLag > lagThreshold) Health.down() else Health.up()
                builder
                    .withDetail("maxLag", maxLag)
                    .withDetail("lagThreshold", lagThreshold)
                    .withDetail("partitionLags", lags)
                    .build()
            }
        } catch (e: Exception) {
            Health.down(e).build()
        }
    }
}
