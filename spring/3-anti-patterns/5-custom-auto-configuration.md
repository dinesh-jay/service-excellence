# Custom Auto-Configuration — Anti-Patterns

## 1. No @ConditionalOnMissingBean

**What people do:** Define beans in auto-configuration without `@ConditionalOnMissingBean`.

**Why it fails:** Consumers who define their own bean get a `BeanDefinitionOverrideException` or unpredictable behavior depending on bean registration order. The starter is no longer a sensible default — it is a mandate.

**Instead:** Always use `@ConditionalOnMissingBean` on every bean in an auto-configuration. The consumer's explicit bean definition always wins.

## 2. Using @ComponentScan in Auto-Configuration

**What people do:** Add `@ComponentScan` to the auto-configuration class to pick up `@Component` and `@Service` classes from the starter module.

**Why it fails:** `@ComponentScan` is greedy — it scans the package and all sub-packages. If the consumer's code is in a parent or sibling package, the scan pulls in unintended beans from the consumer's application. Debugging unexpected bean registrations is painful.

**Instead:** Explicitly declare every bean in the `@AutoConfiguration` class with `@Bean` methods. No scanning, no surprises.

## 3. Not Testing Conditional Logic

**What people do:** Write auto-configuration with `@ConditionalOnClass`, `@ConditionalOnProperty`, etc. but only test the happy path (all conditions met).

**Why it fails:** When a condition is not met, the bean should be absent — but you never verified that. A refactoring breaks a condition, a bean is always created, and the starter forces a dependency that should be optional.

**Instead:** Use `ApplicationContextRunner` to test both paths: condition met (bean present) and condition not met (bean absent). Test overrides too — verify that a user-defined bean replaces the auto-configured one.
