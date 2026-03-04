# Structured Error Handling — Anti-Patterns

## 1. Catching Exceptions in Every Controller

**What people do:** Every controller method has its own try-catch block returning a custom error map.

**Why it fails:** Error response format varies by controller. Some return `{"error": "..."}`, others `{"message": "..."}`, others plain strings. Clients cannot write a single error parser. When a new exception type appears, every controller needs updating.

**Instead:** Use a single `@RestControllerAdvice` with `ProblemDetail`. One place handles all exceptions, one format for all errors.

## 2. Leaking Stack Traces in Responses

**What people do:** Let Spring's default error handling return the full exception stacktrace in the response body.

**Why it fails:** Stack traces expose internal class names, library versions, and SQL queries. This is a security risk (information disclosure) and useless to API consumers.

**Instead:** Handle all exceptions in `@RestControllerAdvice`. Log the full exception server-side. Return a sanitized `ProblemDetail` with a user-facing message.

## 3. Using HTTP Status Codes as the Only Error Signal

**What people do:** Return 400 for every client error, 500 for every server error, with no additional context.

**Why it fails:** Clients cannot distinguish between "validation failed" and "resource not found" — both are 400. Debugging requires reading log timestamps to correlate server logs with client errors.

**Instead:** Use `ProblemDetail.type` as a machine-readable error identifier (e.g., `urn:error:order-not-found`). Clients switch on `type`, not just `status`.
