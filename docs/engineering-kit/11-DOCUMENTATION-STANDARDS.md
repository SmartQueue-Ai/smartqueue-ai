# SmartQueue AI — Documentation Standards

**Document ID:** `EK-11`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

Documentation in SmartQueue AI is treated as a first-class engineering artifact. Clear, accurate, and structured documentation ensures seamless onboarding, maintains architectural consistency across distributed services, and simplifies long-term platform maintenance.

This document establishes the authoring, formatting, structural, and maintenance standards for all documentation committed to this repository.

---

## 2. Documentation Hierarchy & Structure

Documentation is organized across three primary levels:

```text
smartqueue-ai/
├── README.md                           # Main repository landing page & quickstart overview
├── docs/
│   ├── architecture.md                 # System architecture overview & service layout
│   ├── engineering-notes.md            # Technical notes, stack decisions, & environment setups
│   ├── setup-guide.md                  # Comprehensive developer setup & run guide
│   ├── adr/                            # Architecture Decision Records (ADR-0000-*.md)
│   ├── rfc/                            # Request for Comments documents (RFC-0000-*.md)
│   └── engineering-kit/                # Technical governance, standards, & workflows
│       ├── 00-ENGINEERING-CONSTITUTION.md
│       ├── 01-PROJECT-CONTEXT.md
│       └── ...
└── <service-directory>/
    └── README.md                       # Microservice-specific documentation & local setup
```

### 2.1 Top-Level Repository Docs (`docs/`)
- High-level architecture, technology stack notes, deployment guides, ADRs, and RFCs.

### 2.2 Engineering Kit (`docs/engineering-kit/`)
- Formal engineering governance, standards, workflows, checklists, and templates (`EK-00` through `EK-14`).

### 2.3 Microservice Documentation (`<service>/README.md`)
- Every microservice (`backend/auth-service`, `backend/booking-service`, `ai-services`, etc.) **must** contain a dedicated `README.md` documenting:
  - Microservice purpose & domain scope
  - Environment variables & configuration properties
  - Key REST API / WebSocket endpoints
  - RabbitMQ exchanges, queues, and message schemas consumed/produced
  - Local execution & unit test commands

---

## 3. Markdown Syntax & Formatting Rules

All documentation must be written in valid GitHub Flavored Markdown (GFM).

### 3.1 Document Metadata Header
Every formal engineering document in `docs/` must begin with standard document metadata:

```markdown
# SmartQueue AI — Document Title

**Document ID:** `EK-XX` (or `ADR-XXXX` / `RFC-XXXX`)  
**Version:** `1.0.0`  
**Status:** `Active` | `Draft` | `Deprecated`  
**Owner:** SmartQueue AI Engineering Team  

---
```

### 3.2 Heading Hierarchy
- Use ATX-style headings (`#`, `##`, `###`).
- Exactly **one** `#` (H1) title per document.
- Never skip heading levels (e.g., do not jump from `##` to `####`).

### 3.3 Code Blocks & Syntax Highlighting
All code snippets must specify the explicit language tag for syntax highlighting:

````markdown
```java
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController { ... }
```
````

### 3.4 Tables & Lists
- Format tables with clear header rows and alignment delimiters.
- Keep list items concise and structured using dash bullet points (`-`).

### 3.5 Relative Links
- Use relative Markdown links to reference other repository files rather than hardcoded absolute paths or HTTP URLs:
  - *Correct:* `[Git Workflow](./07-GIT-WORKFLOW.md)`
  - *Incorrect:* `[Git Workflow](https://github.com/.../07-GIT-WORKFLOW.md)`

---

## 4. API & Schema Documentation Standards

### 4.1 REST API Specifications (OpenAPI / Swagger)
- Every Spring Boot / FastAPI service exposing REST APIs must auto-generate or maintain OpenAPI 3.0 specifications.
- Annotate controller endpoints with explicit descriptions, request bodies, and response codes (e.g., `@Operation`, `@ApiResponse` in Spring Boot).

### 4.2 Async Messaging & Event Schemas (AsyncAPI / JSON Schema)
- RabbitMQ event payloads and WebSocket messages must be documented with explicit field types, required properties, and JSON schema examples in the service `README.md`.

---

## 5. Inline Code Documentation Standards

Code documentation must explain **why** non-obvious logic exists rather than stating what the syntax does.

### 5.1 Java (Spring Boot)
- Use standard JavaDoc (`/** ... */`) for public interface methods, core domain services, utility classes, and complex concurrency lock routines.
- Annotate concurrency-critical methods explicitly with details on locks held and lock timeout strategies.

### 5.2 Python (FastAPI / AI Services)
- Use Google-style or Sphinx-style docstrings for functions, classes, and module headers.
- Document input parameter types, return types, and exceptions raised.

### 5.3 TypeScript / JavaScript (Node.js / Gateways)
- Use JSDoc annotations for exported functions, types, interfaces, and middleware routines.

---

## 6. Documentation Maintenance & Hygiene

1. **Atomic Documentation Commits:** Any code change that alters API contracts, configuration keys, or workflow procedures **must** include corresponding documentation updates within the exact same Pull Request.
2. **No Dead Links:** Verify all internal relative links operate correctly before submitting PRs.
3. **No Outdated Screenshots:** Prefer structured Markdown tables, code blocks, or text diagrams (Mermaid / ASCII) over binary screenshots that become stale quickly.

---

## 7. Related Documentation

- [Coding Standards](./04-CODING-STANDARDS.md)
- [ADR Guide](./13-ADR-GUIDE.md)
- [RFC Template](./14-RFC-TEMPLATE.md)
