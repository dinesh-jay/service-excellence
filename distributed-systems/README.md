# Distributed Systems

Best practices for building resilient distributed services.

## Philosophy

Distributed systems fail in **partial, unpredictable ways**. A network call can succeed, fail, or hang indefinitely — and you cannot tell which until a timeout fires. Design for failure: isolate blast radius with bulkheads, fail fast with circuit breakers, and degrade gracefully with fallbacks.

Every inter-service call is a liability. Each one introduces latency, a failure mode, and a debugging surface. Make calls resilient by default — retry transient failures, shed load when overwhelmed, and circuit-break when a dependency is down.

## When to Use These Patterns

- **Service-to-service HTTP/gRPC calls** — any synchronous call across a network boundary.
- **Integration with external APIs** — third-party services are unreliable by definition.
- **Microservice architectures** — the more services, the more failure modes.
- **Any system with SLA requirements** — resilience patterns are how you meet uptime guarantees.

## When NOT to Use These Patterns

- **Single-process monolith** — in-process method calls do not need circuit breakers or retries.
- **Fire-and-forget async messaging** — Kafka and message queues handle retries at the infrastructure level.
- **When simplicity matters more** — a startup with two services and no SLA does not need a full resilience stack. Add patterns as failure modes emerge.

## What's in This Library

| Section | Description |
|---|---|
| [1-operational-excellence/](./1-operational-excellence/) | Distributed tracing, cross-service observability, SLOs |
| [2-patterns/](./2-patterns/) | Circuit breaker, retry, rate limiting, bulkhead, service discovery |
| [3-anti-patterns/](./3-anti-patterns/) | Common mistakes and what to do instead |

## Tech Stack

- **Kotlin** — concise, null-safe, interoperable with the Java ecosystem.
- **Spring Boot 4** — framework baseline for all examples.
- **Resilience4j** — lightweight, modular resilience library for the JVM.
- **Micrometer** — metrics from Resilience4j decorators.
- **Gradle Kotlin DSL** — build scripts as code with type-safe accessors.
