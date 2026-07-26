# SmartQueue AI — Pull Request Template

**Document ID:** `EK-09`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

This document specifies the standard Pull Request (PR) format for SmartQueue AI. All pull requests submitted to the repository must strictly follow this structure to ensure comprehensive peer reviews, auditability of system changes, and seamless integration into the `develop` and `main` branches.

---

## 2. GitHub Pull Request Template Location

The raw template snippet in Section 3 is mirrored in `.github/PULL_REQUEST_TEMPLATE.md` to automatically populate the PR description field when creating a pull request on GitHub.

---

## 3. Pull Request Template Markdown Snippet

Below is the standard PR body template. Authors must copy/populate all applicable sections when opening a PR.

```markdown
## 1. Pull Request Summary

**Branch:** `feature/<sprint-or-topic>-<description>`  
**Target Branch:** `develop`  
**Related Issue / Task:** `Closes #SQAI-XXX`  
**Author:** @username  

### Overview of Changes
- High-level summary item 1
- High-level summary item 2
- High-level summary item 3

---

## 2. Type of Change

Select all that apply:

- [ ] `feat`: New feature or business capability
- [ ] `fix`: Bug fix
- [ ] `docs`: Documentation updates only
- [ ] `refactor`: Code restructuring without functional changes
- [ ] `perf`: Performance optimization
- [ ] `test`: Adding or updating test cases
- [ ] `infra`: Infrastructure, Docker Compose, or CI/CD configuration
- [ ] `build`: Dependency updates or build system configuration

---

## 3. Affected Microservices & Components

- [ ] `backend/auth-service`
- [ ] `backend/booking-service`
- [ ] `backend/inventory-service`
- [ ] `backend/realtime-gateway`
- [ ] `ai-services`
- [ ] `infrastructure`
- [ ] `docs`

---

## 4. Technical & Concurrency Impact Analysis

### Concurrency & Data Integrity
- Does this PR modify shared state or inventory reservation logic? **[Yes / No]**
- If yes, describe distributed locking (`SETNX` / Redlock) and transaction isolation controls applied:

### Database & Schema Changes
- Are database migrations included? **[Yes / No / N/A]**
- Details of Liquibase/Flyway schema scripts or index additions:

### Inter-Service Contracts
- Modifies REST API schemas or DTO contracts? **[Yes / No]**
- Modifies RabbitMQ exchange/queue event schemas? **[Yes / No]**

---

## 5. Verification & Testing

### Automated Test Results
- Unit Tests: `[ Pass / Fail ]` (Details / Coverage stats)
- Integration Tests: `[ Pass / Fail ]` (Database & Redis container integration)
- Load / Stress Tests: `[ N/A / Pass ]` (Simulated concurrent users)

### Manual Verification Steps
1. Step 1 to verify functionality locally
2. Step 2 to verify edge cases
3. Observed expected outcome

---

## 6. Definition of Done Checklist

- [ ] Code compiles and passes all local build checks (`mvn clean compile`, `npm run build`).
- [ ] Automated tests have been added/updated and pass successfully.
- [ ] Structured JSON logging with `X-Correlation-ID` is verified for new endpoints/workers.
- [ ] No hardcoded secrets, tokens, or local credentials exist in code or configuration.
- [ ] Relevant documentation in `docs/` or service `README.md` files has been updated.
- [ ] Commits conform to Conventional Commit specification (`docs(workflow): description`).
```

---

## 4. PR Submission Rules

1. **Title Format:** The PR title must strictly match the Conventional Commit format: `<type>(<scope>): <imperative summary>`.  
   *Example:* `feat(inventory): implement redis setnx seat reservation lock`
2. **Single Responsibility:** Keep PRs focused on a single logical task or issue. Split large features into smaller, reviewable PRs.
3. **No Draft PR Merges:** PRs marked as `Draft` cannot be merged until converted to `Ready for Review` and approved.
4. **Clean Git History:** Rebase onto latest `develop` to resolve merge conflicts prior to final review.

---

## 5. Related Documentation

- [Code Review Checklist](./08-CODE-REVIEW-CHECKLIST.md)
- [Git Workflow](./07-GIT-WORKFLOW.md)
- [Commit Message Guidelines](./12-COMMIT-MESSAGE-GUIDELINES.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
