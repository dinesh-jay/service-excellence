# Schema Evolution Example

Demonstrates Avro schema evolution with Confluent Schema Registry.

## Run

```bash
# Start Kafka + Schema Registry
docker-compose up -d

# Generate Avro classes and run
./gradlew bootRun
```

## Test

Send an order event via the REST endpoint:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"order-1","customerId":"cust-1","amount":99.99}'
```

The producer serializes using Avro and registers the schema with the registry.

## Test Schema Evolution

1. Add a new optional field to `OrderEvent.avsc` (e.g., `"region"` with a default).
2. Restart the producer — the new schema is registered automatically.
3. The consumer (running the old schema) still deserializes successfully because the change is backward-compatible.

## Verify Schemas

```bash
# List registered subjects
curl http://localhost:8081/subjects

# Get schema versions
curl http://localhost:8081/subjects/orders-value/versions

# Check compatibility before registering
curl -X POST http://localhost:8081/compatibility/subjects/orders-value/versions/latest \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema": "{...}"}'
```
