# Kafka Best Practices Library — Design

## Goal
Add a Kafka section to the effective-services library covering core production patterns, an opinionated Spring Boot starter, and observability guidance.

## Tech Stack
- Kotlin, Spring Boot 4, spring-kafka
- Gradle Kotlin DSL
- Docker Compose (Confluent Kafka images)
- Avro + Schema Registry for schema-evolution

## Structure
- `kafka/README.md` — philosophy, when to use
- `kafka/patterns/{pattern}/guide.md` — concise & opinionated (~200-400 words)
- `kafka/patterns/{pattern}/anti-patterns.md` — common mistakes with corrections
- `kafka/patterns/{pattern}/example/` — standalone runnable Gradle projects
- `kafka/spring-boot-starter/` — opinionated starter module
- `kafka/observability/` — metrics, tracing, health checks

## Patterns
1. Idempotent Consumer
2. Transactional Outbox
3. Dead Letter Handling
4. Schema Evolution
