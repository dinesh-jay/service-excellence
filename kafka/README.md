# Kafka

Best practices for building production-ready services on Apache Kafka.

## Philosophy

Kafka is an **event streaming platform**, not a message queue. Treat topics as durable, append-only logs that multiple consumers can read independently at their own pace. Design every producer-consumer pipeline for **at-least-once delivery** — exactly-once semantics exist but come with throughput and complexity costs that are rarely justified. Instead, make consumers **idempotent**: if they process the same event twice, the outcome is identical.

## When to Use Kafka

- **Event-driven architectures** — broadcast domain events and let downstream services react asynchronously.
- **Event sourcing** — persist state changes as an immutable sequence of events.
- **Change Data Capture (CDC)** — stream database changes into Kafka via Debezium or similar connectors.
- **High-throughput async communication** — Kafka handles millions of messages per second with horizontal scaling.
- **Service decoupling** — producers and consumers evolve independently; the topic contract is the only shared surface.

## When NOT to Use Kafka

- **Simple request-reply** — use HTTP or gRPC. Kafka adds latency and operational overhead you do not need.
- **Low-volume synchronous calls** — a direct service call is simpler and easier to debug.
- **When a database queue suffices** — a transactional outbox with polling publisher covers many use cases without a distributed broker.
- **When you need strict global ordering** — Kafka guarantees ordering only within a single partition. Funneling all messages through one partition creates a throughput bottleneck.

## What's in This Library

| Section | Description |
|---|---|
| [1-operational-excellence/](./1-operational-excellence/) | Metrics, tracing, health checks, alerting |
| [2-patterns/](./2-patterns/) | Idempotent consumer, transactional outbox, dead-letter handling, schema evolution, out-of-order messages |
| [3-anti-patterns/](./3-anti-patterns/) | Common mistakes and what to do instead |
| [4-spring-boot-starter/](./4-spring-boot-starter/) | Opinionated auto-configuration for producers, consumers, and error handling |

## Tech Stack

- **Kotlin** — concise, null-safe, interoperable with the Java ecosystem.
- **Spring Boot 4** — framework baseline for all starters and examples.
- **spring-kafka** — Spring's first-class Kafka integration.
- **Gradle Kotlin DSL** — build scripts as code with type-safe accessors.
- **Docker Compose** — local Kafka cluster for integration tests and development.
