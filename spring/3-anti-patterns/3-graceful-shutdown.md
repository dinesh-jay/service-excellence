# Graceful Shutdown — Anti-Patterns

## 1. No Shutdown Configuration

**What people do:** Deploy to Kubernetes with default Spring Boot settings. SIGTERM kills the process immediately.

**Why it fails:** In-flight HTTP requests get 502 errors. Database transactions roll back. Kafka consumer offsets are not committed, causing reprocessing on restart.

**Instead:** Set `server.shutdown=graceful` and `spring.lifecycle.timeout-per-shutdown-phase=30s`. Spring drains in-flight requests before stopping.

## 2. Kubernetes terminationGracePeriodSeconds Too Short

**What people do:** Set Spring's shutdown timeout to 30s but leave Kubernetes `terminationGracePeriodSeconds` at the default 30s.

**Why it fails:** Kubernetes sends SIGTERM and starts the 30s countdown. The pre-stop hook, load balancer deregistration, and Spring's drain phase all share that 30s. If they exceed it, Kubernetes sends SIGKILL — ungraceful shutdown.

**Instead:** Set `terminationGracePeriodSeconds` to at least Spring's timeout + pre-stop hook duration + buffer (e.g., 60s).

## 3. Ignoring Non-HTTP Workloads

**What people do:** Enable graceful shutdown for the HTTP server but forget about Kafka consumers, scheduled tasks, and custom thread pools.

**Why it fails:** The HTTP server drains cleanly, but background threads keep running until SIGKILL. Kafka consumer offsets are not committed. Scheduled tasks execute partially.

**Instead:** Use `@PreDestroy` or `DisposableBean` for custom thread pools. Set `setWaitForTasksToCompleteOnShutdown(true)` on `ThreadPoolTaskExecutor`. Kafka consumers shut down through Spring's lifecycle automatically when using spring-kafka.
