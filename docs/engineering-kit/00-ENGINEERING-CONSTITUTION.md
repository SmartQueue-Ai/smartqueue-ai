# SmartQueue AI — Engineering Constitution

**Document ID:** `EK-00`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Preamble & Mandate

SmartQueue AI is an enterprise-grade distributed reservation infrastructure platform built to simulate high-concurrency, flash-sale booking scenarios (such as IRCTC Tatkal, Ticketmaster, redBus, and BookMyShow).

The mission of this repository is to demonstrate production-grade distributed systems engineering, fault-tolerant backend microservices, resilient queue management, robust distributed locking, real-time synchronization, and modern DevOps practices.

This Constitution forms the supreme technical governance for SmartQueue AI. Every line of code, architecture decision, infrastructure manifest, and documentation entry committed to this repository must adhere to the principles set forth herein.

---

## 2. Supreme Technical Values

### 2.1 Production Quality Over Prototype Speed
SmartQueue AI prioritizes production-grade architecture, deep technical rigor, and industrial-strength patterns over rapid hackathon-style prototypes or superficial tutorial implementations. 

### 2.2 Zero Data Corruption & Absolute Concurrency Integrity
In reservation infrastructure, double-booking, state desynchronization, and inventory race conditions are critical system failures. Concurrency integrity, transactional consistency, and idempotent operations are mandatory across all services.

### 2.3 Observability & Infrastructure as First-Class Citizens
Code without observability is non-viable in production. Metrics, structured logging, distributed tracing, containerization, and infrastructure-as-code are treated with the exact same engineering priority as core business logic.

### 2.4 Modular Isolation & Explicit System Contracts
Microservices must maintain strict domain boundaries. Synchronous cross-service coupling is minimized in favor of event-driven asynchronous communication and strict API contracts (REST / WebSockets / RabbitMQ schemas).

### 2.5 Shared Ownership & Peer Accountability
No single component is owned by a single engineer in isolation. All engineers share system-wide understanding, participate in architecture design reviews, enforce standards through code reviews, and share responsibility for system stability.

---

## 3. Non-Negotiable Core Mandates

1. **Strict Concurrency Protection:** All state-modifying operations on limited inventory must utilize verified distributed locks (e.g., Redis `SETNX` / Redlock) and database isolation controls to guarantee zero double-bookings.
2. **Immutable Audit Trails & Idempotency:** Every reservation, booking status change, and payment transaction must be idempotent and backed by an immutable audit trail.
3. **No Direct Pushes to `main` or `develop`:** All changes must enter the codebase via topic/feature branches (`feature/*`), undergo automated verification, and receive peer approval before merging.
4. **Environment Parity:** Local development environments must mirror production using Docker Compose, containerized services (PostgreSQL, Redis, RabbitMQ), and strict configuration parameterization.
5. **No Blind Error Suppression:** Exceptions must be explicitly handled, contextualized, logged with structured correlation identifiers (`X-Correlation-ID`), and never silently swallowed.

---

## 4. Architecture & Technical Governance

- **Architecture Decision Records (ADRs):** Any significant change to system architecture, database choices, state management, or inter-service communication protocols requires an ADR or update to [Architecture Overview](../architecture.md).
- **Technology Stack Drift:** Introducing new core frameworks, database engines, or external dependencies requires team alignment and documentation update in [Engineering Notes](../engineering-notes.md).

---

## 5. Related Documentation & References

- [Architecture Overview](../architecture.md)
- [Engineering Notes](../engineering-notes.md)
- [Setup Guide](../setup-guide.md)
- [Project Context](./01-PROJECT-CONTEXT.md)
- [Engineering Principles](./02-ENGINEERING-PRINCIPLES.md)
