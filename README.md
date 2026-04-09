# riskguard

Transaction risk-scoring REST API built with Spring Boot 3. Each submitted transaction is evaluated against three fraud signals and assigned APPROVED / FLAGGED / REJECTED status automatically.

## Why this project

Demonstrates Java/Spring Boot patterns seen in enterprise and fintech interviews:

| Pattern | Where |
|---|---|
| Spring Security + stateless JWT filter | `config/SecurityConfig.java` |
| `@Transactional` service layer | `service/TransactionService.java` |
| JPA `@OneToMany` / `@ManyToOne` | `model/Account.java`, `model/Transaction.java` |
| Custom `@Query` (JPQL) | `repository/TransactionRepository.java` |
| Bean Validation (`@Valid`, `@NotNull`) | `dto/TransactionRequest.java` |
| `@RestControllerAdvice` global error handling | `exception/GlobalExceptionHandler.java` |
| `@AuthenticationPrincipal` | `controller/TransactionController.java` |

## Risk scoring logic

| Signal | Points |
|---|---|
| Amount ≥ 50,000,000 IDR | +60 |
| Amount ≥ 10,000,000 IDR | +40 |
| ≥ 3 transactions in last 5 minutes | +30 |
| Transaction at 02:00–04:00 WIB | +10 |

- **0–40** → `APPROVED` (HTTP 201)
- **41–70** → `FLAGGED` (HTTP 202, goes to manual review queue)
- **71+** → `REJECTED` (HTTP 422)

## API

```
POST /api/auth/register   { username, password }  → { token, username }
POST /api/auth/login      { username, password }  → { token, username }

POST /api/transactions    { amount, type: CREDIT|DEBIT, description }  → TransactionResponse
GET  /api/transactions    → [ TransactionResponse, ... ]
```

All `/api/transactions` endpoints require `Authorization: Bearer <token>`.

## Run

Requires Java 17+ and Maven.

```bash
./mvnw spring-boot:run
```

H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:riskguard`).

## Example

```bash
# register
curl -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"vincent","password":"secret123"}'

# submit a high-risk transaction (50M IDR → score 60 → FLAGGED)
TOKEN="<token from above>"
curl -X POST localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":50000000,"type":"DEBIT","description":"wire transfer"}'
```
