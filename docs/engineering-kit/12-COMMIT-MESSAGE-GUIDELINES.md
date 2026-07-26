# SmartQueue AI — Commit Message Guidelines

**Document ID:** `EK-12`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

Commit messages serve as the permanent log of architectural and code evolution in SmartQueue AI. Clear, standardized commit messages enable automated changelog generation, facilitate code audits, and streamline peer reviews across distributed teams.

SmartQueue AI strictly enforces the **Conventional Commits 1.0.0** specification.

---

## 2. Commit Message Structure

Every commit message must conform to the following three-part structure:

```text
<type>(<scope>): <short description in imperative mood>

[optional body describing technical motivation and implementation details]

[optional footer(s) listing breaking changes, issue references, or co-authors]
```

---

## 3. Allowed Commit Types

| Type | Purpose | Example Header |
|---|---|---|
| `feat` | New feature or business capability for a microservice | `feat(inventory): add redis SETNX distributed lock` |
| `fix` | Bug fix in service logic, API, or infrastructure | `fix(booking): prevent double seat allocation race condition` |
| `docs` | Documentation changes only | `docs(workflow): add engineering workflow documentation` |
| `refactor` | Code change that neither fixes a bug nor adds a feature | `refactor(auth): simplify JWT payload extraction logic` |
| `perf` | Code change that improves system throughput or latency | `perf(realtime): optimize websocket payload serialization` |
| `test` | Adding missing tests or correcting existing tests | `test(inventory): add concurrent reservation stress test` |
| `infra` | Infrastructure, Docker Compose, or K8s manifest updates | `infra(docker): add rabbitmq cluster container configuration` |
| `ci` | Changes to CI/CD workflows and automation scripts | `ci(github): add automated spring boot test execution workflow` |
| `build` | Build system changes, pom.xml, package.json, or dependencies | `build(deps): upgrade spring boot from 3.2.0 to 3.2.1` |
| `chore` | Routine maintenance tasks or gitignore updates | `chore(gitignore): ignore local log output files` |

---

## 4. Recognized Scopes

The scope indicates the primary microservice or platform component affected by the commit:

| Scope | Target Component / Path |
|---|---|
| `auth` | Auth Service (`backend/auth-service/`) |
| `booking` | Booking Service (`backend/booking-service/`) |
| `inventory` | Inventory Service (`backend/inventory-service/`) |
| `realtime` | Realtime Gateway (`backend/realtime-gateway/`) |
| `ai` | AI Services (`ai-services/`) |
| `frontend` | Web Client / UI (`frontend/`) |
| `infra` | Docker, Kubernetes, Terraform (`infrastructure/`) |
| `docs` | Repository documentation (`docs/`) |
| `workflow` | Engineering process & workflow governance (`docs/engineering-kit/`) |
| `deps` | Third-party dependencies and build configurations |

---

## 5. Subject Line Rules

1. **Imperative Mood:** Write the summary in imperative, present-tense mood (e.g., "add", "fix", "change", NOT "added", "fixes", "changing").
   - *Correct:* `feat(auth): implement refresh token rotation`
   - *Incorrect:* `feat(auth): added refresh token rotation`
2. **Capitalization:** Use lowercase for type, scope, and summary. Do not capitalize the first letter of the subject line unless it starts with a proper noun or acronym.
3. **No Trailing Period:** Do not end the subject line with a period.
4. **Length Restriction:** Limit the first line (header) to **72 characters** or fewer.

---

## 6. Body & Footer Guidelines

### 6.1 Commit Body
- Required for non-trivial features, architectural updates, or subtle bug fixes.
- Explain **what** changed and **why** the change was made, rather than repeating the diff.
- Wrap body text at **72 characters** per line.

### 6.2 Footers
- **Breaking Changes:** Must start with `BREAKING CHANGE:` followed by a space and description of the breaking API or schema change.
- **Issue References:** Reference issue IDs using standard keywords (e.g., `Closes #SQAI-101`, `Fixes #SQAI-204`).

---

## 7. Good vs. Bad Commit Message Examples

| Quality | Commit Message | Reason |
|---|---|---|
| ❌ Bad | `fixed bug` | Vague, missing type/scope, non-imperative. |
| ❌ Bad | `Feat(Booking): Added Redis locks.` | Capitalized type, past tense, trailing period. |
| ❌ Bad | `WIP` | Work-in-progress message; violates specification entirely. |
| ✅ Good | `fix(booking): resolve race condition in seat reservation` | Correct type/scope, imperative mood, under 72 chars. |
| ✅ Good | `docs(workflow): add engineering workflow` | Clean specification, concise scope description. |
| ✅ Good | `feat(inventory): add redlock distributed locking strategy`<br><br>`Implement multi-node Redis lock acquisition with fallback timeout to guarantee reservation concurrency integrity.` | Full header and descriptive body. |

---

## 8. Enforcement & Git Hooks

Commit messages are validated using git commit-msg hooks (`husky` / `commitlint`). Commits that violate the Conventional Commit specification will be automatically rejected locally before staging.

---

## 9. Related Documentation

- [Git Workflow](./07-GIT-WORKFLOW.md)
- [Branching Strategy](./06-BRANCHING-STRATEGY.md)
- [Pull Request Template](./09-PULL-REQUEST-TEMPLATE.md)
