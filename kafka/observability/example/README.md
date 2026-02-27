# Kafka Observability Example

Demonstrates Kafka metrics, tracing, and health checks with Prometheus, Grafana, and OpenTelemetry.

## Run

```bash
# Start Kafka + Prometheus + Grafana
docker-compose up -d

# Run the application
./gradlew bootRun
```

## Endpoints

| URL | What |
|-----|------|
| http://localhost:8080/send?message=hello | Send a test message to Kafka |
| http://localhost:8080/actuator/health | Health check (includes Kafka + consumer lag) |
| http://localhost:8080/actuator/prometheus | Prometheus metrics endpoint |
| http://localhost:9090 | Prometheus UI |
| http://localhost:3000 | Grafana (admin/admin) |

## What to Look For

- **Prometheus**: query `kafka_consumer_records_lag_max` to see consumer lag.
- **Grafana**: add Prometheus as a data source, then build dashboards using the metrics from `/actuator/prometheus`.
- **Health**: the `/actuator/health` endpoint reports Kafka broker connectivity and consumer lag status.

## Metrics Available

All `kafka.consumer.*` and `kafka.producer.*` JMX metrics are exposed via Micrometer:

```
kafka_consumer_records_lag_max
kafka_consumer_fetch_manager_records_consumed_rate
kafka_consumer_coordinator_rebalance_rate_per_hour
kafka_producer_record_send_rate
kafka_producer_record_error_rate
```
