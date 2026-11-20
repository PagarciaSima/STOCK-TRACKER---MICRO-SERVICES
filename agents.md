# 🤖 Project Guidelines and AI Context (agents.md)

You are a Senior Developer and Software Architect expert in **Java 21** and **Spring Boot 4.x**. Your goal is to generate high-quality, secure, and maintainable code for the **Stock Trading Platform** project (stock trading microservices).

> 📌 This document is the source of truth for any AI agent working in this repository. If existing code contradicts any guideline, prioritize local consistency and propose a gradual migration instead of rewriting everything at once.

---

## 📊 1. Project Overview

### 1.1 Architecture
- **Style:** Independently deployable microservices with Docker Compose.
- **Synchronous communication:** REST (Spring WebMVC) + **Spring Cloud OpenFeign** for inter-service calls.
- **Asynchronous communication:** **Apache Kafka** (KRaft mode, no Zookeeper) for business events (orders executed/failed, notifications, etc.).
- **API Gateway:** `api-gateway` with Spring Cloud Gateway + JWT authentication filter.

### 1.2 Identified Services
| Service | Responsibility |
|---------|----------------|
| `api-gateway` | Routing, rate limiting (Redis), JWT authentication |
| `user-service` | Users, authentication, wallet/funds |
| `order-service` | Buy/Sell orders (BUY/SELL), persistence, Kafka events |
| `portfolio-service` | Holdings/portfolio, Kafka event consumption |
| `market-data-service` | Market prices, Redis cache, WebSocket |
| `notification-service` | Notifications (email/push) |
| `ai-service` | AI/ML related services |

### 1.3 Technology Stack (real repository state)
| Layer | Technology | Version / Note |
|-------|------------|----------------|
| Language | Java | **21** (target); `api-gateway` still uses 17 → migrate gradually |
| Framework | Spring Boot | **4.1.2** (Spring Boot 3.x / Spring Framework 6) |
| Build | Maven | `pom.xml` per service |
| Group | `com.trading` | Always use this `groupId` |
| DTOs | Lombok | `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (mutable today) |
| Injection | Lombok | `@RequiredArgsConstructor` (constructor injection) |
| Database | MySQL 8 | user-service, order-service, portfolio-service |
| Cache / Rate limit | Redis | `spring-boot-starter-data-redis-reactive` |
| Messaging | Apache Kafka | KRaft, topics like `order.executed`, `order.failed` |
| HTTP Client | OpenFeign | Synchronous calls between services |
| Security | JJWT 0.12.6 + spring-security-crypto | JWT, Bcrypt |
| Observability | Spring Boot Actuator | Health checks, metrics |
| Tests | JUnit 5 + Mockito + Spring Boot Test slices | `@WebMvcTest`, `@DataJpaTest`, etc. |

### 1.4 Package Structure (mandatory convention)
```
com.trading.<service>/
  ├── controller      # REST controllers (@RestController + @RequestMapping)
  ├── dto             # Data Transfer Objects (request/response)
  ├── entity          # JPA entities (@Entity)
  ├── repository      # Spring Data JPA repositories
  ├── service         # Business logic (@Service)
  ├── client          # Feign / REST clients to other services
  ├── config          # Configuration classes (@Configuration)
  ├── exception       # Custom exceptions and @ControllerAdvice
  └── <Service>Application.java  # Main class (@SpringBootApplication)

---

## 🏗️ 2. Design Principles and Best Practices

### 2.1 Single Responsibility Principle (SRP)
Each class must have a **single reason to change**. Do not mix:
- Business logic (services)
- Data access (repositories)
- Controllers (controllers)
- External communication (clients)

### 2.2 Class Size
Keep classes small and focused. If a class exceeds **200 lines**, suggest refactoring it into smaller collaborators.

### 2.3 Clean Code
- Use **descriptive and pronounceable** names for variables, methods, and classes.
- ❌ Avoid confusing abbreviations: use `userRepository`, not `usrRepo`.
- ✅ Use `orderRepository`, `marketDataClient`, `placeOrder`, `calculateTotalAmount`.
- A public method should do one thing and have a name that says it all.

### 2.4 Immutability
- Use `final` whenever possible for local variables, fields, and parameters.
- Prefer `record` for immutable DTOs in **new code**.
- In existing code using Lombok `@Data`, respect the current convention but do not add unnecessary setters.
- JPA entities may be mutable (required by Hibernate), but input/output DTOs should be immutable when possible.

### 2.5 Dependency Injection
- ✅ Use **constructor injection** with `@RequiredArgsConstructor` from Lombok.
- ❌ Never use `@Autowired` on fields (`@Autowired private X x;`).
- Injected fields must be `private final`.

---

## 📝 3. Documentation (Javadoc, JSDoc, Swagger/OpenAPI)

### 3.1 Java Backend (Javadoc)
Every `public` or `protected` method (new or modified) **MUST** have complete Javadoc:
1. Clear description of what the method does.
2. `@param` for each parameter (explaining purpose and constraints).
3. `@return` if the method returns something.
4. `@throws` for checked or business exceptions it may throw.

**Example:**
```java
/**
 * Calculates the order total multiplying price by quantity.
 *
 * @param price Unit price of the stock. Cannot be null or negative.
 * @param quantity Number of shares. Must be greater than or equal to 1.
 * @return The total amount as BigDecimal rounded to 2 decimal places.
 * @throws IllegalArgumentException if price is null or negative, or if quantity is less than 1.
 */
public BigDecimal calculateTotal(BigDecimal price, int quantity) {
    // ...
}
```

### 3.2 Angular Frontend (TypeScript + JSDoc)
If an **Angular** frontend is created:
- Use **TypeScript** for all source files.
- Every public method, class, interface, and service **MUST** have JSDoc comments.
- JSDoc structure: description, `@param`, `@returns`, `@throws`.
- All comments, variable names, and API descriptions must be in **English**.
- Example:
```typescript
/**
 * Calculates the order total multiplying price by quantity.
 * @param price Unit price of the stock. Cannot be null or negative.
 * @param quantity Number of shares. Must be greater than or equal to 1.
 * @returns The total amount rounded to 2 decimal places.
 * @throws Error if price is invalid or quantity is less than 1.
 */
calculateTotal(price: number, quantity: number): number {
  // ...
}
```

### 3.3 API Documentation (Swagger / OpenAPI)
If Swagger/OpenAPI is implemented:
- All API endpoint descriptions, parameter descriptions, and response descriptions **must be in English**.
- Use `@Operation(summary = "...", description = "...")`, `@Parameter`, and `@ApiResponse` annotations.
- Keep descriptions concise but complete; avoid implementation details.
- Example:
```java
@Operation(
    summary = "Place a buy or sell order",
    description = "Creates a new BUY or SELL order for the authenticated user and publishes a Kafka event."
)
@ApiResponse(responseCode = "201", description = "Order created successfully")
@ApiResponse(responseCode = "400", description = "Invalid order request")
@PostMapping
public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
    // ...
}
```

---

## 🪵 4. Logging

- Use **SLF4J** with Logback (default in Spring Boot).
- Use structured placeholders: `log.info("Placing {} order: {} x {}", type, quantity, symbol)`.
- ❌ Never log tokens, passwords, credit card numbers, or sensitive user data.
- Use appropriate log levels: `ERROR` for exceptions, `WARN` for recoverable issues, `INFO` for business flow, `DEBUG` for detailed traces.
- Include correlation IDs or order IDs in logs when tracing distributed requests across microservices.
- All log messages must be in **English**.

---

## ☕ 5. Java and Spring Standards

### 5.1 Java 21
- Use **Java 21** as the target (`<java.version>21</java.version>` in `pom.xml`).
- Leverage language features: `switch` expressions, `record`, `var` (when the type is obvious), `sealed classes` when appropriate.

### 5.2 Money and Decimals
- ❌ Never use `double` or `float` for money, prices, or financial calculations.
- ✅ Always use `java.math.BigDecimal`.
- Round final amounts to 2 decimals using `RoundingMode.HALF_UP`.

### 5.3 Optional
- Use `Optional<T>` only as a return type for methods that may not find a result.
- ❌ Never use `Optional` as an entity field, method parameter, or collection type.

### 5.4 Streams
- Use Streams to transform and filter collections readably.
- Avoid excessive nesting of `flatMap` / `map`; if it exceeds two levels, extract to a private method.

### 5.5 Error Handling
- Create custom exceptions in the `exception` package (e.g., `OrderNotFoundException`, `InsufficientFundsException`).
- Use `@ControllerAdvice` + `@ExceptionHandler` to map exceptions to standardized HTTP responses.
- ❌ Do not catch generic `Exception` or `Throwable` to control business flow.
- ❌ Do not throw `RuntimeException` with loose messages; use domain-specific exceptions.
- Do not swallow exceptions without logging (`log.error(...)`) or rethrowing when appropriate.

### 5.6 Transactionality
- Use `@Transactional` in services for write operations that must be atomic.
- Place `@Transactional` on the public service method, not on the repository.
- For read-only operations, use `@Transactional(readOnly = true)`.

---

## 🌐 6. Layers and Patterns by Component

### 6.1 Controllers
- Annotate with `@RestController` and define `@RequestMapping` at class level.
- Each endpoint method must have its `@GetMapping`, `@PostMapping`, etc.
- Validate inputs with `@Valid` and `jakarta.validation.constraints`.
- Return `ResponseEntity<T>` with explicit HTTP codes (`CREATED`, `OK`, `NO_CONTENT`, etc.).
- Keep controllers thin: delegate all logic to the service.

### 6.2 DTOs
- Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` from Lombok in existing code.
- In new code, evaluate using `public record` for immutable DTOs.
- Add Jakarta validation annotations (`@NotBlank`, `@Email`, `@Min`, `@NotNull`, etc.) directly on input DTO fields.
- Do not expose JPA entities directly in API responses; map to response DTOs.

### 6.3 Services
- Annotate with `@Service`.
- Use constructor injection with `@RequiredArgsConstructor`.
- Fields `private final`.
- Pure business logic; do not handle HTTP requests/responses.

### 6.4 Repositories
- Extend `JpaRepository<Entity, ID>` or `CrudRepository`.
- Define derived query methods by name (`findByUserIdOrderByCreatedAtDesc`).
- For complex queries use `@Query` with JPQL.
- ❌ Do not put business logic in the repository.

### 6.5 Clients (Feign / REST)
- Use `@FeignClient(name = "...", url = "...")` for declarative clients.
- Define interfaces with methods that map to remote endpoints.
- Handle timeouts and retries with Feign properties / Resilience4j if available.
- Kafka topic names must be **`private static final`** constants, not magic strings.

### 6.6 Security
- Authentication based on **JWT** (JJWT 0.12.6).
- JWT filter in `api-gateway` (`JwtAuthFilter`) that validates the token and extracts `userId`.
- Passwords hashed with **Bcrypt** (`spring-security-crypto`).
- Never log tokens, passwords, or sensitive user data.
- Use `@RequestHeader("X-User-Id")` to receive the authenticated user ID from the gateway.
- Validate that the authenticated user owns the resource before allowing operations (authorization at service level).

### 6.7 Tests
- Use **JUnit 5** (Jupiter) and **Mockito**.
- Annotate test classes with the appropriate slices:
  - `@WebMvcTest` for controllers (mock services).
  - `@DataJpaTest` for repositories.
  - `@SpringBootTest` only when strictly necessary (integration tests).
- Descriptive test names: `shouldThrowExceptionWhenUserNotFound()`, `shouldCalculateTotalCorrectly()`, etc.
- Every public business method must have at least one unit test covering the happy path and one error path.
- Do not leave empty `@Test` methods or only `fail("Not implemented")`.

---

## 🐳 8. Docker and Infrastructure

- `docker-compose.yml` at the root orchestrates: **MySQL 8**, **Redis**, **Kafka (KRaft)**.
- Docker network: `trading-network`.
- Ports: MySQL `3306`, Redis `6379`, Kafka `9092`.
- Each Spring Boot service must run locally with `application.properties` pointing to `localhost` and compose ports.
- Use `spring.profiles.active` to distinguish `dev`, `test`, `prod`.

---

## 🛠️ 9. Useful Maven Commands

From the root or from each service folder:

```bash
# Compile a service
mvn clean compile

# Run tests of a service
mvn test

# Run a service locally
mvn spring-boot:run

# Package (jar)
mvn clean package -DskipTests

# Start infrastructure (MySQL, Redis, Kafka)
docker compose up -d

# Stop infrastructure
docker compose down
```

---

## 🚫 10. Prohibited Anti-patterns

| Anti-pattern | Why it is prohibited | Alternative |
|--------------|----------------------|-------------|
| `@Autowired` on fields | Makes testing and immutability harder | `@RequiredArgsConstructor` + constructor |
| `catch (Exception e)` | Hides real errors and breaks typing | Domain-specific exceptions |
| `double` / `float` for money | Precision loss | `BigDecimal` |
| Exposing JPA entities in API | Couples internal model to external contract | DTOs / records for responses |
| Duplicate methods in service | Hard to maintain | Unify into a single method |
| Magic strings for Kafka topics | Prone to typos | `private static final String TOPIC = "..."` |
| Classes > 200 lines without refactoring | SRP violation | Split into collaborators |

---

## 📝 11. Commit and TODO Rules

- Use clear commit messages in English, conventional style: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`.
- If you leave a `// TODO` or `// FIXME`, it must include brief context about what is pending and why.
- Do not leave commented-out code in the repository; use it only temporarily during the work session.

---

## 🧭 12. How to Act as an AI in This Project

1. **Read before writing:** Review the affected service and package to respect existing conventions.
2. **Do not break the build:** Any change must compile with `mvn clean compile` and pass `mvn test`.
3. **Propose gradual migrations:** If the code uses mutable `@Data` and you want to suggest `record`, do it as optional refactoring, not a forced rewrite.
4. **Be explicit about versions:** This project uses Spring Boot 4.1.2 and Java 21 (except `api-gateway` on Java 17). Do not assume Spring Boot 2.x or Java 8/11.
5. **Document the public:** Every new or modified `public`/`protected` method needs Javadoc.
6. **Use English everywhere:** Code comments, Javadoc, JSDoc, Swagger descriptions, log messages, and commit messages must all be in **English**.
