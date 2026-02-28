# Temporal Spring Boot Starter

Opinionated auto-configuration for Temporal with Spring Boot 4.

## What It Provides

- **WorkflowClient bean** — configured from `application.yml` properties.
- **WorkerFactory bean** — auto-discovers workflow and activity implementations.
- **Jackson data converter** — Kotlin-friendly serialization with `jackson-module-kotlin`.
- **Health indicator** — checks Temporal server connectivity via Actuator.
- **Graceful shutdown** — `WorkerFactory` shuts down cleanly on application stop.

## Opinionated Defaults

| Setting | Value | Why |
|---------|-------|-----|
| Data converter | Jackson JSON | Kotlin data classes serialize cleanly |
| Namespace | `default` | Single namespace for simplicity; override in production |
| Shutdown | Graceful | `WorkerFactory.shutdown()` on bean destroy |

All beans are `@ConditionalOnMissingBean`. Define your own to replace defaults.

---

## Code

### Configuration Properties

```kotlin
@ConfigurationProperties(prefix = "app.temporal")
data class TemporalStarterProperties(
    val serviceAddress: String = "localhost:7233",
    val namespace: String = "default",
    val taskQueue: String = "default-queue",
)
```

### Auto-Configuration

```kotlin
@AutoConfiguration
@EnableConfigurationProperties(TemporalStarterProperties::class)
class TemporalStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun workflowServiceStubs(properties: TemporalStarterProperties): WorkflowServiceStubs {
        return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget(properties.serviceAddress)
                .build()
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun workflowClient(
        stubs: WorkflowServiceStubs,
        properties: TemporalStarterProperties,
    ): WorkflowClient {
        return WorkflowClient.newInstance(stubs,
            WorkflowClientOptions.newBuilder()
                .setNamespace(properties.namespace)
                .setDataConverter(JacksonJsonDataConverter.newDefaultInstance())
                .build()
        )
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    fun workerFactory(client: WorkflowClient): WorkerFactory {
        return WorkerFactory.newInstance(client)
    }
}
```

### Usage Configuration

```yaml
app:
  temporal:
    service-address: localhost:7233
    namespace: default
    task-queue: my-task-queue
```

### Worker Registration

Register workflows and activities manually or via component scanning:

```kotlin
@Component
class WorkerRegistrar(
    private val workerFactory: WorkerFactory,
    private val properties: TemporalStarterProperties,
    private val activities: List<Any>,
) {
    @PostConstruct
    fun register() {
        val worker = workerFactory.newWorker(properties.taskQueue)
        worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
        activities.forEach { worker.registerActivitiesImplementations(it) }
        workerFactory.start()
    }
}
```
