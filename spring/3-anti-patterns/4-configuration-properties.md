# Configuration Properties — Anti-Patterns

## 1. Using @Value Everywhere

**What people do:** Inject properties with `@Value("${app.feature.timeout}")` directly in service classes.

**Why it fails:** No type safety — a typo in the property name fails silently with a null or default. No validation — a negative timeout or missing required value is only caught at runtime. Properties are scattered across the codebase with no single source of truth.

**Instead:** Use `@ConfigurationProperties` with a data class. One class per concern, validated at startup, discoverable in one place.

## 2. One Giant Configuration Class

**What people do:** Create a single `AppProperties` class that holds every property in the application.

**Why it fails:** The class grows into a god object. Changes to unrelated features touch the same file. Testing requires constructing the entire config even when you only need one property. Auto-complete shows 50 properties when you need one.

**Instead:** One `@ConfigurationProperties` class per concern: `OrderProperties`, `PaymentProperties`, `NotificationProperties`. Each class is small, focused, and independently testable.

## 3. No Startup Validation

**What people do:** Define configuration properties without `@Validated` or validation annotations.

**Why it fails:** A missing required property or invalid value is not caught until the code path executes — possibly in production, possibly at 2 AM. The error message is a `NullPointerException` deep in business logic, not a clear "property X is required".

**Instead:** Add `@Validated` to the properties class and use `@NotBlank`, `@Positive`, `@URL` etc. The application fails fast at startup with a clear message.
