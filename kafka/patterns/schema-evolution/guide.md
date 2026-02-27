# Schema Evolution

## What

Evolving message schemas over time without breaking existing consumers. Producers and consumers deploy independently — a schema change in the producer must not cause deserialization failures in running consumers.

## Why

Without a schema contract, breaking changes are discovered at runtime. A renamed field, a removed required field, or a type change causes deserialization exceptions that poison your consumer. With schema enforcement, incompatible changes are rejected at publish time.

## How

Use **Avro + Confluent Schema Registry**. The registry stores versioned schemas and enforces compatibility rules before allowing a new schema version to be registered.

### Compatibility Types

| Mode | Rule | Use When |
|------|------|----------|
| **BACKWARD** | New schema can read data written with the old schema | Consumers upgrade before producers |
| **FORWARD** | Old schema can read data written with the new schema | Producers upgrade before consumers |
| **FULL** | Both backward and forward compatible | Independent deployments |
| **NONE** | No checks | Never in production |

**Recommendation:** Use `BACKWARD_TRANSITIVE` (checked against all prior versions, not just the latest).

### Safe Changes

- Add a field **with a default value** — old consumers ignore it, new consumers use the default for old messages.
- Remove a field **that had a default** — old consumers fall back to the default when the field is missing.

### Unsafe Changes

- Remove a required field (no default) — old consumers fail.
- Rename a field — Avro treats this as a remove + add; old consumers lose the data.
- Change a field's type — deserialization fails.

## Without Avro

If you use JSON, embed a `version` field in the payload. Deserialize into a versioned DTO hierarchy and use a deserializer chain that tries the latest version first, then falls back.

## See Also

- [Runnable example](./example/) — Avro producer/consumer with Schema Registry
- [Anti-patterns](./anti-patterns.md) — common mistakes
