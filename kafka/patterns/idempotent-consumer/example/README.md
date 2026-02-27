# Idempotent Consumer Example

Demonstrates deduplication using a JPA-backed processed message store.

## Run

```bash
# Start Kafka
docker-compose up -d

# Run the application
./gradlew bootRun
```

## Test

Produce a message to the `orders` topic:

```bash
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic orders \
  --property "parse.headers=true" \
  --property "headers.delimiter=|" \
  --property "headers.separator=," \
  <<< 'message_id:order-123|{"orderId":"order-123","customerId":"cust-1","amount":99.99}'
```

Send the same message again — the consumer logs that it skips the duplicate.

## What to Look For

- `ProcessedMessage` table in H2 tracks every processed message ID.
- Duplicate messages are detected and skipped before business logic runs.
- Offsets are committed only after successful processing.
