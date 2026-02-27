# Transactional Outbox Example

Demonstrates the polling-based transactional outbox pattern with Spring Boot 4 and JPA.

## Run

```bash
# Start Kafka
docker-compose up -d

# Run the application
./gradlew bootRun
```

## What Happens

1. `OrderService.createOrder()` saves an `Order` and writes an `OutboxEvent` in the same transaction.
2. `OutboxPublisher` polls for unpublished events every 5 seconds and sends them to Kafka.
3. Published events are marked with a `publishedAt` timestamp (not deleted).

## What to Look For

- Check the H2 console (`/h2-console`, JDBC URL `jdbc:h2:mem:outbox`) to see outbox rows transition from `published_at = null` to a timestamp.
- The Kafka topic `order-events` receives the events reliably, even if Kafka was temporarily down when the order was created.
