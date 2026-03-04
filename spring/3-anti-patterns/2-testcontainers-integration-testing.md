# Integration Testing — Anti-Patterns

## 1. Mocking the Database

**What people do:** Mock `JdbcTemplate` or `JpaRepository` in service tests and verify method calls.

**Why it fails:** Mocks don't catch SQL syntax errors, missing columns, wrong transaction isolation, or N+1 queries. The test passes but the code fails against a real database.

**Instead:** Use Testcontainers with `@DataJpaTest` or `@SpringBootTest`. Test against a real PostgreSQL instance that matches production.

## 2. Sharing a Single Test Database Across All Tests

**What people do:** Point all tests at a shared H2 or PostgreSQL instance without isolation.

**Why it fails:** Tests leak state. Test A inserts rows that cause Test B to fail. Tests pass individually but fail when run together. Order-dependent test suites are a maintenance nightmare.

**Instead:** Use Testcontainers with a fresh container per test class (or `@Transactional` to roll back after each test). Each test starts with a clean state.

## 3. Using H2 as a Test Database

**What people do:** Use H2 in-memory mode for tests because it is fast and requires no Docker.

**Why it fails:** H2 is not PostgreSQL. Window functions, JSON operators, CTEs, and many DDL features differ. Tests pass on H2 and fail on the real database. You end up maintaining two sets of SQL — one for H2 compatibility and one for production.

**Instead:** Use Testcontainers with the same database engine and version as production. The 5-10 second startup cost is worth the confidence.
