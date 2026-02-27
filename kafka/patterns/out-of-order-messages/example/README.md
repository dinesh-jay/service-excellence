# Out-of-Order Messages Example

Demonstrates sequence-number-based reordering with a buffered event store.

## Run

```bash
# Start Kafka
docker-compose up -d

# Run the application
./gradlew bootRun
```

## Test

Send events out of order for the same orderId (sequence 1, 3, 2):

```bash
# Sequence 1 — CREATED (processed immediately)
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic order-events \
  <<< '{"orderId":"order-1","sequenceNumber":1,"eventType":"CREATED","payload":"{\"status\":\"new\"}"}'

# Sequence 3 — SHIPPED (arrives before 2, gets buffered)
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic order-events \
  <<< '{"orderId":"order-1","sequenceNumber":3,"eventType":"SHIPPED","payload":"{\"status\":\"shipped\"}"}'

# Sequence 2 — UPDATED (fills the gap, triggers processing of both 2 and buffered 3)
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic order-events \
  <<< '{"orderId":"order-1","sequenceNumber":2,"eventType":"UPDATED","payload":"{\"status\":\"confirmed\"}"}'
```

## What to Look For

- Sequence 1 processes immediately (CREATED).
- Sequence 3 is buffered because sequence 2 has not arrived yet.
- When sequence 2 arrives, it processes immediately, then the inline drain picks up buffered sequence 3.
- Check H2 console (`/h2-console`, JDBC URL `jdbc:h2:mem:ordering`) to see `processing_state` and `buffered_events` tables.
