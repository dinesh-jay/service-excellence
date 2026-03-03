# Graceful Shutdown

## What

When the application receives a shutdown signal (SIGTERM), it stops accepting new requests, drains in-flight requests, and then exits cleanly. No requests are dropped. No database transactions are left half-committed.

## Why

In Kubernetes, a rolling deployment sends SIGTERM to the old pod while routing traffic to the new one. Without graceful shutdown, in-flight requests get killed mid-execution — users see 502s, database transactions roll back, and Kafka offsets may not commit.

## How

Spring Boot 4 supports graceful shutdown natively. Enable it in configuration, set a timeout, and coordinate with the health check so the load balancer stops sending traffic before the server stops accepting it.

The shutdown sequence:
1. SIGTERM received
2. Readiness probe returns DOWN — load balancer stops routing new traffic
3. Server stops accepting new connections
4. In-flight requests complete (up to the timeout)
5. Application context closes, beans are destroyed
6. Process exits

## Key Considerations

- **Set a shutdown timeout** — without a timeout, a stuck request blocks shutdown indefinitely. 30s is a reasonable default.
- **Coordinate with Kubernetes** — set `terminationGracePeriodSeconds` in the pod spec to be longer than the Spring shutdown timeout.
- **Drain non-HTTP work** — Kafka consumers, scheduled tasks, and thread pools also need graceful shutdown. Use `@PreDestroy` or `DisposableBean`.
- **Pre-stop hook** — add a short sleep (5s) in the Kubernetes pre-stop hook to give the load balancer time to deregister the pod before Spring starts draining.

---

## Code

### Enable Graceful Shutdown

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Kubernetes Deployment Snippet

```yaml
spec:
  terminationGracePeriodSeconds: 60
  containers:
    - name: app
      lifecycle:
        preStop:
          exec:
            command: ["sleep", "5"]
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 8080
        periodSeconds: 5
      livenessProbe:
        httpGet:
          path: /actuator/health/liveness
          port: 8080
        periodSeconds: 10
```

### Graceful Shutdown for Custom Thread Pool

```kotlin
@Configuration
class AsyncConfig {

    @Bean(destroyMethod = "shutdown")
    fun taskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 8
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)
            setThreadNamePrefix("async-")
        }
    }
}
```
