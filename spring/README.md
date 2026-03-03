# Spring Boot

Best practices for building production-ready services with Spring Boot 4.

## Philosophy

Spring Boot is an **opinionated framework**. Lean into its conventions. The framework has sensible defaults for configuration, error handling, health checks, and metrics — override them only when you understand what the default does and why it doesn't fit your case.

Prefer **auto-configuration over manual wiring**. Use `@ConfigurationProperties` over `@Value`. Test with real dependencies (Testcontainers) over mocks. Let Spring manage the lifecycle — fight the framework and you lose.

## When to Use Spring Boot

- **REST APIs and web services** — embedded server, auto-configured Jackson, exception handling out of the box.
- **Microservices** — Actuator for observability, externalized configuration, Docker-friendly fat JARs.
- **Event-driven services** — first-class integration with Kafka (spring-kafka), RabbitMQ (spring-amqp), and Temporal.
- **Batch processing** — Spring Batch with restart, skip, and retry built in.
- **Any JVM service that needs production readiness** — health checks, metrics, tracing, graceful shutdown come free.

## When NOT to Use Spring Boot

- **Serverless functions with cold-start constraints** — Spring's startup overhead (1-3s) is too slow. Use Micronaut, Quarkus, or plain Kotlin.
- **Libraries or SDKs** — Spring Boot is for applications, not libraries. A library should not force Spring on its consumers.
- **Simple CLI tools** — a framework designed for long-running services is overkill for a script.
- **When the team doesn't know Spring** — Spring's magic (auto-configuration, proxies, AOP) is productive when understood, but a debugging nightmare when not.

## What's in This Library

| Section | Description |
|---|---|
| [1-operational-excellence/](./1-operational-excellence/) | Actuator, metrics, health checks, structured logging |
| [2-patterns/](./2-patterns/) | Error handling, integration testing, graceful shutdown, configuration, auto-configuration |
| [3-anti-patterns/](./3-anti-patterns/) | Common Spring Boot mistakes and what to do instead |

## Tech Stack

- **Kotlin** — concise, null-safe, interoperable with the Java ecosystem.
- **Spring Boot 4** — framework baseline for all examples.
- **Testcontainers** — real dependencies in integration tests.
- **Micrometer** — vendor-neutral metrics facade.
- **Gradle Kotlin DSL** — build scripts as code with type-safe accessors.
