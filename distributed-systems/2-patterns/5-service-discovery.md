# Service Discovery

## What

Services find each other dynamically at runtime instead of using hardcoded URLs. A service registry maintains the list of available instances and their network locations. Callers query the registry to get a healthy instance.

## Why

Hardcoded URLs break when services scale (new instances), move (new IPs), or fail (dead instances). Updating URLs requires redeployment. Service discovery makes the system self-healing: new instances register automatically, failed instances are deregistered, and callers always get a healthy target.

## How

Two approaches:

**Client-side discovery** — the caller queries the registry and selects an instance. Spring Cloud `DiscoveryClient` + `@LoadBalanced RestClient` handles this. The caller has full control over load balancing.

**Server-side discovery** — a load balancer sits between the caller and the registry. The caller sends requests to the load balancer, which routes to a healthy instance. Kubernetes Services work this way.

In Kubernetes, prefer server-side discovery (Kubernetes Services). Outside Kubernetes, use client-side discovery with Spring Cloud.

## Key Considerations

- **Kubernetes-native** — if you are on Kubernetes, use Kubernetes Services. No need for Eureka, Consul, or other registries. DNS-based discovery is built in.
- **Health checks** — the registry must remove unhealthy instances. In Kubernetes, readiness probes handle this. In Eureka, configure heartbeat intervals and eviction.
- **Client-side load balancing** — Spring Cloud LoadBalancer supports round-robin and random. For more sophisticated strategies, configure a custom `ServiceInstanceListSupplier`.
- **DNS caching** — JVM caches DNS lookups. In Kubernetes, set `networkaddress.cache.ttl=10` to pick up new pods quickly.

---

## Code

### Kubernetes Service Discovery (Server-Side)

No code needed — Kubernetes DNS resolves service names automatically:

```yaml
# application.yml
app:
  payment-service:
    url: http://payment-service.default.svc.cluster.local:8080
```

```kotlin
@Service
class PaymentClient(
    @Value("\${app.payment-service.url}") private val baseUrl: String,
) {
    private val restClient = RestClient.builder().baseUrl(baseUrl).build()

    fun chargePayment(request: PaymentRequest): PaymentResponse {
        return restClient.post()
            .uri("/payments")
            .body(request)
            .retrieve()
            .body(PaymentResponse::class.java)!!
    }
}
```

### Spring Cloud Client-Side Discovery

```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-all")
    // or for non-Kubernetes:
    // implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}
```

```kotlin
@Configuration
class RestClientConfig {

    @Bean
    @LoadBalanced
    fun restClientBuilder(): RestClient.Builder {
        return RestClient.builder()
    }
}
```

```kotlin
@Service
class PaymentClient(
    restClientBuilder: RestClient.Builder,
) {
    // "payment-service" resolves via the service registry
    private val restClient = restClientBuilder.baseUrl("http://payment-service").build()

    fun chargePayment(request: PaymentRequest): PaymentResponse {
        return restClient.post()
            .uri("/payments")
            .body(request)
            .retrieve()
            .body(PaymentResponse::class.java)!!
    }
}
```

### JVM DNS Cache Configuration

```kotlin
// In application startup or via JVM args: -Dnetworkaddress.cache.ttl=10
Security.setProperty("networkaddress.cache.ttl", "10")
```
