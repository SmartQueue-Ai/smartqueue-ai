# SmartQueue AI — Coding Standards

**Document ID:** `EK-04`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

SmartQueue AI is a multi-technology platform comprising Java (Spring Boot), TypeScript/Node.js, Python (FastAPI), SQL (PostgreSQL), and Redis. This document establishes uniform coding standards across all supported technology stacks to ensure code readability, maintainability, and enterprise-grade reliability.

---

## 2. Java & Spring Boot Standards

### 2.1 Package Structure Layering
Each Spring Boot microservice (`auth-service`, `booking-service`, `inventory-service`) must adhere to strict layered architecture:

```text
com.smartqueue.<service>/
├── controller/         # REST API Endpoints & Request Mapping
├── service/            # Business Logic & Orchestration
│   └── impl/           # Service Implementations (where applicable)
├── repository/         # Spring Data JPA / Database Access Layer
├── domain/             # JPA Entities & Aggregate Roots
├── dto/                # Request & Response Data Transfer Objects
├── config/             # Spring Security, Redis, RabbitMQ Beans
└── exception/          # Custom Domain Exceptions & Global Handler
```

### 2.2 Domain Entities vs DTO Isolation
- **Entities Never Leaked:** Database entities (`@Entity`) must never be returned directly in REST API controllers or accepted as HTTP request bodies.
- **DTO Usage:** Define separate Request DTOs (with Bean Validation annotations like `@NotNull`, `@Min`) and Response DTOs.
- **Immutability:** DTOs should be immutable where possible (Java `record` types or Lombok `@Value`/`@Builder`).

### 2.3 Exception Handling
- **Global Controller Advice:** Use `@RestControllerAdvice` to translate domain exceptions into standardized JSON error responses.
- **Standard Error Payload:**
  ```json
  {
    "timestamp": "2026-07-26T22:45:00Z",
    "status": 409,
    "error": "Conflict",
    "message": "Seat A-102 is currently locked by another reservation",
    "path": "/api/v1/seats/reserve",
    "correlationId": "req-987a-4b2c-8821"
  }
  ```

---

## 3. TypeScript & Node.js Standards

### 3.1 Strict Mode & Static Typing
- `tsconfig.json` must enforce `"strict": true`.
- **No `any` Types:** Explicit `any` or implicit `any` is prohibited. Use `unknown`, generics, or union types with proper runtime type guards.

### 3.2 Async Programming & Error Handling
- Use `async/await` syntax for all asynchronous operations. Raw Promises or callback chains should be avoided.
- All async calls must be wrapped in `try/catch` blocks or handled via centralized error middleware in Express/Socket.IO handlers.

---

## 4. Python & AI Services Standards

### 4.1 Type Hints & Pydantic
- All Python functions in `ai-services/` must include type hints (`typing` module).
- Request payloads, response formats, and internal data structures must be defined using Pydantic models (`BaseModel`).

### 4.2 FastAPI Best Practices
- Asynchronous route handlers (`async def`) must be used for non-blocking I/O operations.
- Dependencies (such as database connections or RAG clients) must be injected using FastAPI's `Depends` mechanism.

---

## 5. Database & Persistence Standards

### 5.1 Relational Database (PostgreSQL)
- **Migrations:** All schema changes must be executed via automated migration files (Flyway / Liquibase). Manual DDL statements in production or dev databases are strictly forbidden.
- **Transactions:** Use explicit transaction management (`@Transactional` in Spring Boot) for multi-step mutations. Keep transaction boundaries as short as possible to prevent lock contention under high load.

### 5.2 Cache & Distributed Locking (Redis)
- **Key Naming Convention:** All Redis keys must follow the structured format:
  `smartqueue:<service>:<entity>:<identifier>[:attribute]`
  - *Example 1:* `smartqueue:inventory:seat-lock:flight-101:seat-12B`
  - *Example 2:* `smartqueue:auth:user-session:usr_99812`
- **TTL Requirement:** Every key inserted into Redis must have an explicit Time-To-Live (TTL) configured to prevent memory leaks and dangling distributed locks.

---

## 6. Logging & Tracing Standards

- **Structured Logging:** Log output must be formatted as structured JSON in non-local environments.
- **Log Levels:**
  - `ERROR`: System failures requiring immediate investigation (DB down, unhandled exceptions).
  - `WARN`: Recoverable degradation or expected edge cases (seat lock conflict, rate limit exceeded).
  - `INFO`: Significant lifecycle events (service started, booking confirmed).
  - `DEBUG`: Detailed diagnostic data (disabled in production).
- **Correlation ID Propagation:** All incoming requests must extract or generate an `X-Correlation-ID` header. This ID must be passed to downstream microservices, RabbitMQ message metadata, and log contexts (`MDC`).

---

## 7. Related Documentation

- [Repository Standards](./03-REPOSITORY-STANDARDS.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
- [Engineering Principles](./02-ENGINEERING-PRINCIPLES.md)
