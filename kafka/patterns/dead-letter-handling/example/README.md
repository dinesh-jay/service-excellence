# Dead Letter Handling Example

Demonstrates DLT routing with retry and exponential backoff using Spring Kafka's `DefaultErrorHandler`.

## Run

```bash
# Start Kafka
docker-compose up -d

# Run the application
./gradlew bootRun
```

## Test

Send a valid message:

```bash
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic orders \
  <<< '{"orderId":"order-1","customerId":"cust-1","amount":50.00}'
```

Send a poison pill (invalid JSON):

```bash
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-producer --broker-list localhost:9092 --topic orders \
  <<< 'not-valid-json'
```

## What to Look For

- The valid message is processed successfully.
- The poison pill is retried 3 times (1s, 2s, 4s backoff), then routed to `orders.DLT`.
- The `DltConsumer` logs the failed message with original topic, partition, offset, and exception details.

Read the DLT topic:

```bash
docker exec -it $(docker ps -q -f ancestor=confluentinc/cp-kafka:7.7.0) \
  kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.DLT --from-beginning
```
