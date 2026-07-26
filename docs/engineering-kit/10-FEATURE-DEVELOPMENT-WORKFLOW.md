# SmartQueue AI — Feature Development Workflow

**Document ID:** `EK-10`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

This document outlines the standardized end-to-end lifecycle for feature development within SmartQueue AI. Following this structured workflow ensures architectural alignment, prevents state corruption under high concurrency, maintains environment parity, and guarantees production-grade reliability across all microservices.

---

## 2. End-to-End Feature Lifecycle Overview

```text
┌─────────────────┐    ┌──────────────────┐    ┌───────────────────┐    ┌───────────────────┐
│ 1. Architecture │───>│ 2. Branch & Env  │───>│ 3. Development    │───>│ 4. Verification   │
│    & RFC / ADR  │    │    Preparation   │    │    & TDD          │    │    & Observability│
└─────────────────┘    └──────────────────┘    └───────────────────┘    └───────────────────┘
                                                                                  │
┌─────────────────┐    ┌──────────────────┐    ┌───────────────────┐              │
│ 7. Post-Merge   │<───│ 6. Merge & CI/CD │<───│ 5. Code Review    │<─────────────┘
│    Cleanup      │    │    Deployment    │    │    & Iteration    │
└─────────────────┘    └──────────────────┘    └───────────────────┘
```

---

## 3. Workflow Stages Detailed

### Stage 1: Architecture & Technical Planning

Before writing code for major system modifications:

1. **Evaluate Architectural Scope:** Determine if the task introduces a new microservice, changes cross-service API/message schemas, or alters concurrency locking strategies.
2. **RFC / ADR Requirement:**
   - If introducing major architectural changes or new infrastructure components, author an **RFC** using the [RFC Template](./14-RFC-TEMPLATE.md).
   - If making a definitive architectural trade-off choice (e.g., Redlock vs database pessimistic locking), document it via an **ADR** following the [ADR Guide](./13-ADR-GUIDE.md).
3. **Task Decomposition:** Breakdown the feature into small, independently testable units.

---

### Stage 2: Branch Creation & Environment Preparation

1. **Synchronize Target Branch:**
   ```bash
   git checkout develop
   git pull origin develop
   ```
2. **Cut Feature Branch:** Create a short-lived branch adhering to [Branching Strategy](./06-BRANCHING-STRATEGY.md):
   ```bash
   git checkout -b feature/sprint<X>-<feature-short-name>
   ```
3. **Start Local Environment:** Ensure local infrastructure containers (PostgreSQL, Redis, RabbitMQ) are running via Docker Compose:
   ```bash
   docker compose up -d
   ```

---

### Stage 3: Test-Driven & Modular Development

1. **Domain Layer First:** Define domain entities, value objects, and repository interfaces. Ensure DB entities are kept isolated from external API schemas.
2. **Concurrency Controls:** If modifying inventory or seat reservation states, implement distributed locking mechanisms (`SETNX` / Redlock) and idempotency key handling before business logic.
3. **Write Unit Tests:** Implement comprehensive unit tests targeting domain rules, boundary cases, and failure paths.
4. **Implement Service & Controller Layers:** Write clean, modular implementation code fulfilling the API contracts.

---

### Stage 4: Observability, Logging & Local Verification

1. **Inject Structured Logging:** Add structured JSON logging with MDC (Mapped Diagnostic Context) propagation for `X-Correlation-ID` across HTTP headers and AMQP message metadata.
2. **Add Metrics & Health Checks:** Register custom Actuator/Micrometer counters, timers, or gauges for critical operations (e.g., reservation attempts, lock acquisition failures).
3. **Execute Local Test Suites:** Run full automated test suites and verify integration against real Dockerized PostgreSQL/Redis instances.

---

### Stage 5: Self-Review & PR Submission

1. **Execute Pre-Review Checklist:** Perform author self-review using the [Code Review Checklist](./08-CODE-REVIEW-CHECKLIST.md).
2. **Commit Changes:** Format commits according to [Commit Message Guidelines](./12-COMMIT-MESSAGE-GUIDELINES.md):
   ```bash
   git add .
   git commit -m "feat(inventory): add redis lock mechanism for seat reservations"
   ```
3. **Push & Create PR:** Push the feature branch to remote and submit a PR to `develop` using the [Pull Request Template](./09-PULL-REQUEST-TEMPLATE.md).

---

### Stage 6: Peer Code Review & CI Validation

1. **Automated Verification:** GitHub Actions CI automatically triggers build pipelines, linter execution, and test suites.
2. **Peer Review:** At least one designated reviewer evaluates the code against [Code Review Checklist](./08-CODE-REVIEW-CHECKLIST.md).
3. **Address Feedback:** Incorporate reviewer feedback in logical follow-up commits. Avoid force-pushing while active review is underway.

---

### Stage 7: Merge, Deployment & Post-Merge Cleanup

1. **PR Merge:** Once approved and all CI checks pass, the PR is merged into `develop` using a rebase or squash-and-merge strategy.
2. **Branch Deletion:** Delete the remote feature branch on GitHub and prune local references:
   ```bash
   git checkout develop
   git pull origin develop
   git branch -d feature/sprint<X>-<feature-short-name>
   git remote prune origin
   ```
3. **Definition of Done Verification:** Confirm all criteria in [Definition of Done](./05-DEFINITION-OF-DONE.md) are satisfied.

---

## 4. Key Rules & Best Practices

- **Never Commit Secrets:** Do not commit `.env` files, API credentials, or certificates.
- **Short Branch Lifespans:** Feature branches must be merged within 1–3 days to prevent code drift and complex merge conflicts.
- **Continuous Integration Sync:** Rebase feature branches on `develop` daily (`git rebase origin/develop`).

---

## 5. Related Documentation

- [Branching Strategy](./06-BRANCHING-STRATEGY.md)
- [Git Workflow](./07-GIT-WORKFLOW.md)
- [Code Review Checklist](./08-CODE-REVIEW-CHECKLIST.md)
- [ADR Guide](./13-ADR-GUIDE.md)
- [RFC Template](./14-RFC-TEMPLATE.md)
