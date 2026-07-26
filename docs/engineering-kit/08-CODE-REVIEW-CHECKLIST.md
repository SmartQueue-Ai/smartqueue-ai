# SmartQueue AI — Code Review Checklist

**Document ID:** `EK-08`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

Code reviews are a vital quality gate in SmartQueue AI. Because the platform simulates high-concurrency reservation infrastructure handling flash-sale throughput, code reviews must go beyond surface-level syntax checks. Reviewers must evaluate architectural alignment, concurrency protection, state integrity, security, observability, and performance.

This checklist provides a structured review process for both authors (self-check before PR submission) and reviewers (peer evaluation prior to merge).

---

## 2. Pre-Review Author Self-Check

Before requesting peer review on a Pull Request, the author **must** verify:

- [ ] Branch is cut from and synchronized with latest `develop`.
- [ ] Code compiles locally without warnings or errors.
- [ ] Automated unit and integration tests pass (`mvn test`, `pytest`, `npm test`).
- [ ] No temporary debug code, hardcoded credentials, print statements, or unused imports remain.
- [ ] PR description follows [Pull Request Template](./09-PULL-REQUEST-TEMPLATE.md).
- [ ] Commit history is clean and conforms to [Commit Message Guidelines](./12-COMMIT-MESSAGE-GUIDELINES.md).

---

## 3. Reviewer Expectations & SLA

- **Response Time:** Initial review within 24 hours of PR assignment during business days.
- **Constructive Tone:** Feedback must be objective, actionable, and focused on code quality rather than personal coding style preferences.
- **Clear Demarcation:** Distinguish mandatory blocking changes from optional suggestions using explicit prefixes:
  - `[BLOCKING]`: Must be resolved prior to approval.
  - `[NIT]`: Minor cosmetic/formatting suggestion; non-blocking.
  - `[QUESTION]`: Request for clarification or context.

---

## 4. Technical Review Checklist

### 4.1 Architecture & Domain Isolation
- [ ] **Domain Boundaries:** Code changes respect microservice domain boundaries (`auth-service`, `booking-service`, `inventory-service`, `realtime-gateway`, `ai-services`).
- [ ] **No Direct Cross-DB Access:** Services do not access foreign databases directly; all cross-service communication occurs via validated REST APIs or RabbitMQ event schemas.
- [ ] **DTO / Entity Separation:** Database entities (`@Entity`) are not leaked directly in API controllers or external schemas. Request/Response DTOs are used explicitly.

### 4.2 Concurrency & State Integrity
- [ ] **Distributed Locking:** Operations modifying shared inventory or booking state enforce distributed locks (e.g., Redis `SETNX` / Redlock) with proper TTLs and automatic release in `finally` blocks.
- [ ] **Idempotency:** State-modifying endpoints (reservations, payment processing) implement explicit idempotency keys to handle retry spikes without duplicate side effects.
- [ ] **Transaction Isolation:** Relational database operations utilize appropriate `@Transactional` boundaries and isolation levels to avoid dirty or phantom reads under high load.
- [ ] **Race Conditions:** Counter increments, status updates, and quota checks are atomic and immune to race conditions.

### 4.3 Security & Authentication
- [ ] **JWT Validation:** Protected API routes validate JWT signatures, expiration, and user scope/role claims.
- [ ] **Input Sanitization & Validation:** All incoming user inputs are validated using Bean Validation annotations (`@NotNull`, `@Size`, `@Pattern`) or Pydantic/Zod schemas.
- [ ] **SQL Injection & XSS Prevention:** Prepared statements or ORM parameters are used exclusively; no raw string concatenation in SQL queries or HTML responses.
- [ ] **Secrets Management:** No API keys, database credentials, JWT secrets, or private tokens are hardcoded. Environment variables or secrets managers are used.

### 4.4 Performance & Resource Management
- [ ] **Database Query Optimization:** No N+1 query patterns; appropriate fetching strategies (`JOIN FETCH`, entity graphs) and indexes exist for new query criteria.
- [ ] **Resource Cleanup:** Database connections, HTTP clients, socket connections, and file handles are properly closed or managed via connection pools.
- [ ] **Async Non-Blocking Execution:** Time-consuming operations (email notifications, queue processing) are offloaded to background threads or RabbitMQ message queues.

### 4.5 Observability & Logging
- [ ] **Structured Logging:** Logs use structured JSON format and include correlation identifiers (`X-Correlation-ID`) across incoming and outgoing calls.
- [ ] **Appropriate Log Levels:** `ERROR` for system failures requiring intervention, `WARN` for recoverable unexpected states, `INFO` for major domain state changes, `DEBUG` for troubleshooting.
- [ ] **No Sensitive Data Logging:** Passwords, JWTs, personal identifiable information (PII), and credit card details are never logged.

### 4.6 Testing & Documentation
- [ ] **Test Coverage:** Unit tests cover happy paths, edge cases, error conditions, and concurrency limits.
- [ ] **Clean Test Code:** Test cases are independent, deterministic, and free of arbitrary sleeping/delays.
- [ ] **Documentation Updates:** Inline documentation (JavaDoc / Docstrings), OpenAPI specs, and relevant files in `docs/` are updated to reflect behavior changes.

---

## 5. Approval & Merge Requirements

A PR is eligible for merging into `develop` only when:

1. At least **one** peer review approval is granted by a senior engineer or domain code owner.
2. All automated CI checks (builds, linters, test suites) pass with zero errors.
3. All blocking comment threads (`[BLOCKING]`) are resolved by the author and verified by the reviewer.
4. The branch is rebased on latest `develop` with a clean commit history.

---

## 6. Related Documentation

- [Pull Request Template](./09-PULL-REQUEST-TEMPLATE.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
- [Coding Standards](./04-CODING-STANDARDS.md)
- [Git Workflow](./07-GIT-WORKFLOW.md)
