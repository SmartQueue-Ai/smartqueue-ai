# SmartQueue AI — Branching Strategy

**Document ID:** `EK-06`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

SmartQueue AI uses a structured branching model designed to simulate real-world, enterprise software engineering environments. This model ensures code stability, protects production branches, enables parallel feature development, and facilitates rigorous code reviews.

---

## 2. Core Branching Model

```text
  main (Production Releases)
   ▲
   │ (Release Merges & Tags: v1.0.0)
   │
  develop (Sprint Integration)
   ▲        ▲        ▲
   │        │        │
   │        │        └──────────────────────┐
   │        └──────────────┐                │
  feature/auth-jwt   feature/inventory   feature/realtime
```

---

## 3. Branch Taxonomy & Purpose

### 3.1 Primary Long-Lived Branches

1. **`main` Branch:**
   - **Purpose:** Production-ready code.
   - **Stability:** Extremely stable. Direct commits or force pushes are **strictly forbidden**.
   - **Tags:** All merges into `main` are tagged with Semantic Versioning (e.g., `v1.0.0`, `v1.1.0`).

2. **`develop` Branch:**
   - **Purpose:** Central integration branch for current sprint features and engineering tasks.
   - **Stability:** Stable and buildable at all times. Direct pushes are disabled; changes enter only via approved Pull Requests from feature branches.

### 3.2 Supporting Short-Lived Branches

3. **`feature/*` Branches:**
   - **Purpose:** Development of new microservices, platform features, or infrastructure capabilities.
   - **Parent Branch:** Cut from `develop`.
   - **Merge Target:** Merges back into `develop`.
   - **Naming Convention:** `feature/<sprint-or-topic>-<short-description>`  
     *Examples:* `feature/sprint0-engineering-foundation`, `feature/inventory-redis-lock`, `feature/auth-jwt-bootstrap`

4. **`bugfix/*` Branches:**
   - **Purpose:** Fixing non-critical bugs found during integration testing in `develop`.
   - **Parent Branch:** Cut from `develop`.
   - **Merge Target:** Merges back into `develop`.
   - **Naming Convention:** `bugfix/<ticket-id>-<short-description>`  
     *Example:* `bugfix/sq-104-fix-token-expiry`

5. **`hotfix/*` Branches:**
   - **Purpose:** Critical fixes for production issues identified in `main`.
   - **Parent Branch:** Cut from `main`.
   - **Merge Target:** Merges into **both** `main` and `develop`.
   - **Naming Convention:** `hotfix/v<version>-<short-description>`  
     *Example:* `hotfix/v1.0.1-redis-lock-leak`

6. **`infrastructure/*` Branches:**
   - **Purpose:** Dedicated DevOps, Docker, Kubernetes, or CI/CD configuration updates.
   - **Parent Branch:** Cut from `develop`.
   - **Merge Target:** Merges back into `develop`.
   - **Naming Convention:** `infrastructure/<topic-description>`  
     *Example:* `infrastructure/day0-docker-setup`

---

## 4. Branch Hygiene & Lifecycle Rules

1. **Short-Lived Lifetime:** Feature branches should ideally exist for 1–3 days. Avoid long-running branches to minimize complex merge conflicts.
2. **Syncing with Target:** Feature branches must regularly merge or rebase latest changes from `develop` (`git pull origin develop`).
3. **Branch Deletion:** Upon merging a Pull Request into `develop` or `main`, the source feature/bugfix branch must be immediately deleted locally and remotely.

---

## 5. Release & Versioning Lifecycle

- **Semantic Versioning (SemVer 2.0.0):** Version numbers follow `vMAJOR.MINOR.PATCH`.
  - `MAJOR`: Incompatible API or architectural breaking changes.
  - `MINOR`: Backward-compatible new functionality or microservices.
  - `PATCH`: Backward-compatible bug fixes and security patches.
- **Release Process:** When a sprint milestone is completed in `develop`, a release PR is submitted to `main`. After approval and CI execution, `main` is tagged.

---

## 6. Related Documentation

- [Engineering Notes](../engineering-notes.md)
- [Git Workflow](./07-GIT-WORKFLOW.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
