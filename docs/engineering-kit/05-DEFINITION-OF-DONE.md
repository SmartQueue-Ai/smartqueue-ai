# SmartQueue AI — Definition of Done (DoD)

**Document ID:** `EK-05`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

The Definition of Done (DoD) is the official quality contract for SmartQueue AI. A feature, bug fix, refactoring effort, or infrastructure task is only considered **complete** when it satisfies all mandatory criteria outlined in this document. No pull request may be merged into `develop` or `main` without satisfying the DoD.

---

## 2. Definition of Done Checklist

### 2.1 Code & Architecture Quality
- [ ] Code complies strictly with [Coding Standards](./04-CODING-STANDARDS.md) and [Repository Standards](./03-REPOSITORY-STANDARDS.md).
- [ ] No hardcoded secrets, database credentials, hostnames, or API keys exist in source code.
- [ ] Microservice boundaries and domain responsibilities are maintained without inappropriate inter-service coupling.
- [ ] Concurrency-sensitive operations (such as seat reservations) explicitly implement locking mechanisms (Redis `SETNX`, DB locks) and idempotency guards.
- [ ] All new public endpoints and methods are clearly structured and free of dead/commented-out code.

### 2.2 Testing & Quality Assurance
- [ ] **Unit Tests:** All unit tests pass locally and in CI. Minimum 80% line coverage for critical business logic services.
- [ ] **Integration Tests:** Integration tests pass against containerized database (PostgreSQL), cache (Redis), and queue (RabbitMQ) instances.
- [ ] **Concurrency Tests:** Any state modification logic involving inventory or reservation queues includes synthetic concurrent test coverage verifying zero double-bookings.
- [ ] **Regression:** No existing tests fail or are disabled/commented out to bypass CI.

### 2.3 Infrastructure & Containerization
- [ ] Application service builds cleanly inside its `Dockerfile`.
- [ ] `docker-compose.yml` (and override files) start the service and all dependencies without errors.
- [ ] Service health check endpoint (`/actuator/health`, `/health`, or `/healthz`) returns HTTP 200 `UP` status.
- [ ] New environment variables are declared with default values in `.env.example` and documented in setup guides.

### 2.4 Observability & Exception Handling
- [ ] Structured logging is implemented using standard log levels (`INFO`, `WARN`, `ERROR`).
- [ ] Correlation ID (`X-Correlation-ID`) propagation is verified across request paths and async message boundaries.
- [ ] Exception handlers translate failures into standardized HTTP status codes and JSON error bodies.

### 2.5 Documentation & Architecture Parity
- [ ] Architecture documentation in [Architecture Overview](../architecture.md) or [Engineering Notes](../engineering-notes.md) is updated if system topology, schema, or workflow changes occurred.
- [ ] OpenAPI / Swagger specifications or API contracts are updated for modified REST/WebSocket interfaces.
- [ ] Relevant README files within the target microservice folder are updated.

### 2.6 Version Control & Merge Requirements
- [ ] Commits adhere to Conventional Commit format (see [Git Workflow](./07-GIT-WORKFLOW.md)).
- [ ] Feature branch is up to date with the target branch (`develop`).
- [ ] Pull Request description contains clear summary, testing proof, and completed DoD checklist.
- [ ] Approved by at least one peer reviewer.
- [ ] All automated CI/CD pipeline checks pass cleanly.

---

## 3. Enforcement & Accountability

- **Pull Request Blocking:** Automated GitHub Actions workflows and peer reviewers must enforce these DoD checks.
- **Exceptions:** Temporary waivers for non-critical criteria (e.g., deferring a load test to a follow-up ticket) require explicit rationale recorded in the Pull Request comments and approved by the engineering owner.

---

## 4. Related Documentation

- [Engineering Constitution](./00-ENGINEERING-CONSTITUTION.md)
- [Coding Standards](./04-CODING-STANDARDS.md)
- [Branching Strategy](./06-BRANCHING-STRATEGY.md)
- [Git Workflow](./07-GIT-WORKFLOW.md)
