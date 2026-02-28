# Schema Evolution — Anti-Patterns

## 1. No Schema Registry

**What people do:** Serialize messages as raw JSON strings with no schema contract. Producers and consumers agree on the shape "by convention" or by reading the same data class.

**Why it fails:** There is no enforcement. A producer adds a field, renames one, or changes a type, and the first sign of trouble is a deserialization exception in production.

**Instead:** Use a Schema Registry (Confluent, Apicurio, or AWS Glue). Register schemas, enforce compatibility rules, and let the serializer/deserializer handle schema resolution automatically.

## 2. Using NONE Compatibility

**What people do:** Set the Schema Registry compatibility mode to `NONE` because "we control all consumers" or "it's easier during development."

**Why it fails:** `NONE` allows any schema change — including breaking ones. It defeats the purpose of having a registry. The moment a breaking change slips through, consumers fail at runtime with no prior warning.

**Instead:** Use `BACKWARD_TRANSITIVE` as the default. Only use `NONE` in development environments, never in staging or production.

## 3. Renaming or Removing Required Fields

**What people do:** Rename `user_id` to `userId` or remove a field that had no default value, assuming consumers will be updated simultaneously.

**Why it fails:** Avro treats a rename as a delete + add. Old consumers cannot find the original field and deserialization fails. Removing a required field means old consumers have no fallback value.

**Instead:** Add the new field with a default and deprecate the old one. Once all consumers have migrated, remove the old field in a subsequent schema version — only if it had a default.
