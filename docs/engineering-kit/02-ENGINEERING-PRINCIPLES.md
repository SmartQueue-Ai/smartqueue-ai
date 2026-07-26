# SmartQueue AI — Engineering Principles

**Document ID:** `EK-02`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

This document defines the core technical philosophy and engineering principles guiding all software development on SmartQueue AI. These principles bridge high-level architecture vision with daily implementation practices.

---

## 2. Fundamental Engineering Principles

### Principle 1: Infrastructure First

Infrastructure, containerization, and system architecture are treated as foundational requirements, not post-development additions.

- Environment repeatability and Docker container orchestration are established before application business logic.
- Service networking, database migrations, configuration injection, and health checks are required for every service on day one.
- **Rationale:** Poor technical foundations create compound technical debt that degrades velocity and stability as services scale.

---

### Principle 2: Production Thinking

Every engineering implementation—whether a database query, REST endpoint, event publisher, or caching strategy—must answer five core production questions before merge:

1. **Purpose:** *Why does this exist, and what concrete problem does it solve?*
2. **Scalability Impact:** *What bottleneck or concurrency issue does this address?*
3. **High-Load Behavior:** *How will this component behave under 10,000 requests per second or network degradation?*
4. **Production Debuggability:** *How can an engineer diagnose a failure in production using logs and metrics?*
5. **Horizontal Scaling:** *Can this service scale out statelessly without breaking data consistency?*

---

### Principle 3: Shared System Ownership

SmartQueue AI rejects siloed code ownership ("my component" vs "your component").

- Every contributor understands the end-to-end architecture, from infrastructure containers to real-time WebSocket events.
- Code review is collaborative, rigorous, and educational.
- Engineers actively maintain and review components outside their immediate focus area.
- **Rationale:** Shared ownership prevents single points of failure in team knowledge and raises overall code quality.

---

### Principle 4: Concurrency & Defensive State Management

In a reservation infrastructure platform, concurrent state mutations are the default state of execution, not an edge case.

- Never assume single-threaded execution or serialized HTTP requests.
- Use explicit locking mechanisms (Redis `SETNX`, PostgreSQL pessimistic/optimistic locks) when mutating shared resources.
- Guarantee idempotency on all POST/PUT endpoints and asynchronous event listeners.
- **Rationale:** Race conditions in ticket allocation or inventory management result in double-booking catastrophes.

---

### Principle 5: Fail-Safe Asynchronous Decoupling

Synchronous microservice-to-microservice HTTP calls during critical hot paths create cascading failures.

- Use RabbitMQ for event-driven workflows (booking created, payment status updated, notification trigger).
- Implement Dead Letter Queues (DLQ) and exponential backoff retry strategies for all async consumers.
- Ensure state persistence occurs before event publishing (Outbox Pattern where necessary).

---

### Principle 6: Observability-Driven Development

Systems must be inspectable by default.

- All log output must be structured (JSON format in production) and include correlation IDs (`X-Correlation-ID`) across service boundaries.
- Error logs must include sufficient contextual metadata (user context, resource IDs, execution trace) without leaking sensitive secrets or PII.
- Standard health check endpoints (`/actuator/health`, `/healthz`) are mandatory for container orchestration probe compatibility.

---

## 3. Decision Matrix for Code & Design Reviews

When reviewing pull requests or evaluating design choices, use the following matrix:

| Criterion | Acceptable | Unacceptable |
|---|---|---|
| **Architecture** | Explicit boundaries, async queues, stateless handlers | Tight REST coupling, monolithic database access |
| **Concurrency** | Atomic locks, idempotency keys, DB isolation | In-memory un-synchronized state, unchecked increments |
| **Error Handling** | Custom domain exceptions, structured error responses | Swallowed exceptions, raw stack traces to clients |
| **Configuration** | Environment variables, `.env.example` mapping | Hardcoded URLs, ports, or credentials |
| **Testing** | Unit & integration test coverage for core paths | Unchecked code paths, deleted or skipped tests |

---

## 4. Related Documentation

- [Architecture Overview](../architecture.md)
- [Engineering Notes](../engineering-notes.md)
- [Engineering Constitution](./00-ENGINEERING-CONSTITUTION.md)
- [Coding Standards](./04-CODING-STANDARDS.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
