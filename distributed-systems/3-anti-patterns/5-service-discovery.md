# Service Discovery — Anti-Patterns

## 1. Hardcoded Service URLs

**What people do:** Put IP addresses or hostnames directly in configuration: `http://10.0.1.42:8080/api`.

**Why it fails:** When the service moves to a new IP (scaling, migration, restart), the caller breaks. Updating requires redeployment of every caller. In dynamic environments (Kubernetes, auto-scaling), IPs change constantly.

**Instead:** Use DNS-based service discovery (Kubernetes Services) or a service registry (Eureka, Consul). Services register themselves and callers resolve by name, not IP.

## 2. Long DNS Cache TTL

**What people do:** Use the JVM's default DNS cache (which may cache indefinitely) or set a long TTL.

**Why it fails:** When a service instance dies and a new one starts with a different IP, the caller keeps sending traffic to the dead IP because the DNS entry is cached. The JVM's default `networkaddress.cache.ttl` is 30s for successful lookups, but some environments override this to infinity.

**Instead:** Set `networkaddress.cache.ttl=10` in Kubernetes environments. DNS should resolve fresh every 10 seconds to pick up new pods.

## 3. No Health Checks on Registered Instances

**What people do:** Register service instances but do not configure health checks in the registry.

**Why it fails:** A crashed instance stays registered. The load balancer or client-side discovery sends traffic to a dead instance, getting timeouts or connection refused errors. Users see intermittent failures that are hard to diagnose because "some requests work and some don't."

**Instead:** Configure health checks at every level: readiness probes in Kubernetes, heartbeats in Eureka, health endpoints in the service. Unhealthy instances must be deregistered within seconds.
