# Map-Reduce Child Workflows Example

Demonstrates batch order processing using Temporal child workflows for parallel fan-out/fan-in.

## Run

```bash
# Start Temporal server + PostgreSQL + UI
docker-compose up -d

# Wait for Temporal to be ready (~15 seconds)
# Temporal UI available at http://localhost:8080

# Run the application
./gradlew bootRun
```

## What Happens

1. On startup, `BatchProcessRunner` submits a `BatchProcessWorkflow` with 20 sample order IDs.
2. The parent workflow chunks the orders into batches of 5 and starts a `ProcessOrderWorkflow` child for each chunk.
3. Each child workflow processes its orders sequentially, calling activities to validate, charge, and fulfill each order.
4. The parent collects all child results and returns a `BatchResult` summary.
5. The runner logs the final result.

## What to Look For

- Open the Temporal UI at `http://localhost:8080` to see the parent workflow and its child workflow executions.
- Click into a child workflow to see the individual activity calls (validateOrder, chargePayment, fulfillOrder).
- Check application logs to see processing progress and the final summary.
- Try stopping the app mid-processing and restarting — the workflows resume from where they left off.
