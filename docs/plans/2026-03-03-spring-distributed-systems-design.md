# Design: Spring & Distributed Systems Sections

## Context

The service-excellence project has top-level sections for `kafka` and `temporal`, each with sub-folders for operational excellence, patterns, anti-patterns, and a Spring Boot starter. We're adding two new sections: `spring` (Spring Boot framework patterns) and `distributed-systems` (infrastructure/resilience patterns).

## Spring Section

### Structure

```
spring/
  README.md
  1-operational-excellence/README.md
  2-patterns/
  3-anti-patterns/
```

No `4-spring-boot-starter` — this section IS Spring Boot.

### README

Philosophy: Spring Boot is an opinionated framework. Lean into its conventions. Override defaults only when you understand what the default does and why it doesn't fit. Covers: Actuator, metrics, health, graceful shutdown, testing, error handling, configuration.

### Operational Excellence

Actuator endpoints, Micrometer metrics, custom health indicators, info contributors, structured logging with correlation IDs.

### Patterns (5 files)

1. **Structured error handling** — `@ControllerAdvice` + `ProblemDetail` (RFC 9457), consistent error responses across all endpoints.
2. **Testcontainers integration testing** — `@ServiceConnection`, reusable containers, slice tests vs full context tests.
3. **Graceful shutdown** — shutdown hooks, in-flight request draining, health check coordination with load balancers.
4. **Configuration properties** — `@ConfigurationProperties` with `@Validated`, profiles, externalized config hierarchy.
5. **Custom auto-configuration** — writing your own starter with `@AutoConfiguration`, `@ConditionalOnMissingBean`, META-INF registration.

### Anti-Patterns (5 files, mirror the patterns)

1. Raw exceptions without `@ControllerAdvice`
2. Mocking everything instead of integration testing
3. Hard shutdown without draining
4. `@Value` everywhere instead of typed config
5. Monolithic configuration classes

## Distributed Systems Section

### Structure

```
distributed-systems/
  README.md
  1-operational-excellence/README.md
  2-patterns/
  3-anti-patterns/
```

No `4-spring-boot-starter` — patterns use various libraries (Resilience4j, Spring Cloud) with code inline in each file.

### README

Philosophy: distributed systems fail in partial, unpredictable ways. Design for failure by isolating blast radius, failing fast, and degrading gracefully. All examples use Resilience4j with Spring Boot.

### Operational Excellence

Distributed tracing with OpenTelemetry, cross-service correlation, SLO-based alerting, Resilience4j metrics in Micrometer.

### Patterns (5 files)

1. **Circuit breaker** — Resilience4j circuit breaker, state transitions (closed/open/half-open), fallback strategies, configuration.
2. **Retry with backoff** — Resilience4j retry, exponential backoff, jitter, retry budgets to prevent retry storms.
3. **Rate limiting** — Resilience4j rate limiter, token bucket, client-side vs server-side, coordination across instances.
4. **Bulkhead isolation** — Thread pool and semaphore bulkheads, preventing one slow dependency from cascading.
5. **Service discovery** — Client-side vs server-side, Spring Cloud DiscoveryClient, Kubernetes-native discovery.

### Anti-Patterns (5 files, mirror the patterns)

1. No circuit breaker — cascading failures across services
2. Retrying without backoff or budget — retry storms amplify failures
3. No rate limiting — thundering herd on recovery
4. Shared thread pools — one slow service blocks everything
5. Hardcoded service URLs — brittle, non-portable deployments

## Conventions

All files follow existing project conventions:
- Pattern files: What / Why / How / Key Considerations / Code (at bottom)
- Anti-pattern files: same structure, showing the mistake and the fix
- Code examples: Kotlin, Spring Boot 4, Gradle Kotlin DSL
- All config beans use `@ConditionalOnMissingBean`
