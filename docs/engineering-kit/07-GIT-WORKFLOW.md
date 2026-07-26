# SmartQueue AI — Git Workflow & Conventional Commits

**Document ID:** `EK-07`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

This document specifies the standard Git execution workflow and commit message standards for SmartQueue AI. Adherence to these standards ensures clear commit histories, automated changelog generation, and seamless peer collaboration.

---

## 2. Conventional Commit Specification

All commit messages in the SmartQueue AI repository **must** conform to the Conventional Commits specification.

### 2.1 Commit Message Structure

```text
<type>(<scope>): <short description in imperative mood>

[optional body describing motivation and implementation details]

[optional footer(s) listing breaking changes or issue references]
```

### 2.2 Allowed Commit Types

| Type | Purpose | Example |
|---|---|---|
| `feat` | Adding a new feature or microservice component | `feat(inventory): add redis SETNX distributed locking` |
| `fix` | Fixing a bug in business logic or infrastructure | `fix(auth): handle expired JWT token edge case` |
| `docs` | Documentation changes only | `docs(engineering): add engineering foundation` |
| `refactor` | Code change that neither fixes a bug nor adds a feature | `refactor(booking): simplify state machine validation` |
| `perf` | Performance optimization change | `perf(realtime): optimize socket event serialization` |
| `test` | Adding or updating tests | `test(inventory): add concurrent seat reservation test` |
| `build` | Changes to build tools, dependencies, or Maven/npm scripts | `build(deps): upgrade Spring Boot to version 3.2.1` |
| `ci` | Changes to CI/CD configuration files (GitHub Actions) | `ci(github): add automated maven test workflow` |
| `infra` | Infrastructure changes (Docker Compose, K8s, scripts) | `infra(docker): add redis container service` |
| `chore` | Maintenance tasks or general updates | `chore(gitignore): update ignored build patterns` |

### 2.3 Recognized Scopes

Common scopes used across the project:

- `auth`: Auth Service (`backend/auth-service/`)
- `booking`: Booking Service (`backend/booking-service/`)
- `inventory`: Inventory Service (`backend/inventory-service/`)
- `realtime`: Realtime Gateway (`backend/realtime-gateway/`)
- `ai`: AI Services (`ai-services/`)
- `frontend`: Frontend Web Client (`frontend/`)
- `infra`: Docker, Compose, Kubernetes (`infrastructure/`)
- `docs`: Architecture & Engineering Kit (`docs/`)
- `engineering`: Standards, policies, and process configuration
- `deps`: Third-party dependencies and build configurations

---

## 3. Step-by-Step Feature Workflow

Follow this procedure for every task or feature:

### Step 1: Update Local `develop` Branch
Before starting work, ensure your local `develop` branch is synchronized with remote `origin`:

```bash
git checkout develop
git pull origin develop
```

### Step 2: Create a Feature Branch
Create a short-lived branch using standard branch naming conventions (see [Branching Strategy](./06-BRANCHING-STRATEGY.md)):

```bash
git checkout -b feature/sprint0-engineering-foundation
```

### Step 3: Make Incremental, Logical Commits
Stage specific files and commit changes with a valid Conventional Commit message:

```bash
git add docs/engineering-kit/
git commit -m "docs(engineering): add engineering foundation"
```

### Step 4: Synchronize Before Pushing
Before pushing, ensure your feature branch is up to date with any recent merges to `develop`:

```bash
git fetch origin
git rebase origin/develop
```

*(If merge conflicts occur during rebase, resolve them locally, stage the files with `git add`, and run `git rebase --continue`.)*

### Step 5: Push Feature Branch to Remote
Push your branch to GitHub and set upstream tracking:

```bash
git push -u origin feature/sprint0-engineering-foundation
```

### Step 6: Create Pull Request & Complete Definition of Done
1. Open a Pull Request from your feature branch into `develop` on GitHub.
2. Fill out the PR template, specifying:
   - Summary of changes
   - Technical motivation
   - Empirical verification proof (test results, command outputs)
   - Completed [Definition of Done](./05-DEFINITION-OF-DONE.md) checklist.
3. Request review from a peer engineer.

---

## 4. Troubleshooting Common Git Scenarios

### Scenario A: Push Rejected (Remote Contains Work You Do Not Have)
If `git push` fails because origin has newer commits on `develop`:

```bash
git checkout develop
git pull origin develop
git checkout feature/your-feature-branch
git rebase develop
git push origin feature/your-feature-branch
```

### Scenario B: Amending the Last Commit
If you need to fix a typo or add a missed file to the most recent commit before pushing:

```bash
git add <filename>
git commit --amend --no-edit
```

---

## 5. Related Documentation

- [Branching Strategy](./06-BRANCHING-STRATEGY.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
- [Engineering Notes](../engineering-notes.md)
