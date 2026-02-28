# Temporal

Best practices for building production-ready services with Temporal.

## Philosophy

Temporal is a **durable execution engine**, not a task queue. Workflows are reliable by default — retries, timeouts, and state persistence are built in. Think of workflows as functions that survive process crashes. A workflow function's local variables persist across failures, deployments, and infrastructure outages. You write straight-line code; Temporal handles the durability.

Design every workflow for **determinism**: the same input must always produce the same sequence of commands. Side effects (HTTP calls, database writes, random numbers, current time) belong in **activities**, not workflow code.

## When to Use Temporal

- **Long-running processes** — operations that span minutes, hours, or days (e.g., order fulfillment, onboarding pipelines).
- **Saga orchestration** — coordinate compensating transactions across multiple services.
- **Scheduled jobs** — cron-like schedules with built-in failure handling, replacing fragile cron + scripts setups.
- **Fan-out / fan-in** — process N items in parallel using child workflows and aggregate results.
- **Human-in-the-loop workflows** — pause a workflow and wait for a signal (approval, callback).
- **Distributed transactions** — transactional guarantees across service boundaries without two-phase commit.

## When NOT to Use Temporal

- **Simple CRUD** — if the operation is a single database write behind an API, Temporal adds unnecessary complexity.
- **Sub-second latency requirements** — Temporal adds scheduling overhead (typically 50-200ms). Use direct service calls for hot paths.
- **Stateless request-reply** — synchronous HTTP/gRPC calls are simpler and faster.
- **When a message queue suffices** — if you just need async fire-and-forget with retries, Kafka or SQS is lighter.

## What's in This Library

| Section | Description |
|---|---|
| [patterns/](./patterns/) | Map-reduce with child workflows |
| [anti-patterns/](./anti-patterns/) | Common mistakes and what to do instead |
| [operational-excellence/](./operational-excellence/) | Metrics, visibility, health checks |
| [spring-boot-starter/](./spring-boot-starter/) | Opinionated auto-configuration for Temporal with Spring Boot 4 |

## Tech Stack

- **Kotlin** — concise, null-safe, interoperable with the Java ecosystem.
- **Temporal Java SDK** — Temporal's first-class Java/Kotlin integration.
- **Spring Boot 4** — framework baseline for all examples.
- **Gradle Kotlin DSL** — build scripts as code with type-safe accessors.
- **Docker Compose** — local Temporal server for integration tests and development.
