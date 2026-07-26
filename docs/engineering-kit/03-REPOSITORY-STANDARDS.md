# SmartQueue AI — Repository Standards

**Document ID:** `EK-03`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Overview

This document specifies the organizational standards, file layout conventions, configuration rules, and dependency management policies for the SmartQueue AI monorepo repository.

---

## 2. Monorepo Directory Organization

```text
smartqueue-ai/
├── .env.example            # Root template for environment configuration
├── .gitignore              # Repository-wide Git ignore rules
├── LICENSE                 # Repository license terms
├── README.md               # Monorepo landing documentation
├── backend/                # Spring Boot microservices & Node.js gateway
│   ├── auth-service/       # Identity, JWT, RBAC service
│   ├── booking-service/    # Booking orchestration service
│   ├── inventory-service/  # Seat inventory & locking service
│   └── realtime-gateway/   # Socket.IO real-time notification gateway
├── ai-services/            # Python FastAPI AI, recommendations & RAG
├── frontend/               # Next.js web application
├── infrastructure/         # Docker Compose, K8s manifests & deployment scripts
├── docs/                   # Platform documentation & Engineering Kit
│   └── engineering-kit/    # Engineering governance & process specifications
└── scripts/                # Utility scripts for local setup, seeding, & testing
```

---

## 3. Directory & Service Responsibilities

1. **`backend/` Directory:**
   - Must contain autonomous microservices. Each microservice must be runnable independently or via Docker Compose.
   - Each Java Spring Boot microservice (`auth-service`, `booking-service`, `inventory-service`) must include its own build definition (`pom.xml` or `build.gradle`), `Dockerfile`, and application configuration (`application.yml`).
   - The `realtime-gateway` must contain its own `package.json`, TypeScript config, and `Dockerfile`.

2. **`ai-services/` Directory:**
   - Python FastAPI application modules for machine learning, semantic search, and RAG pipelines.
   - Must include `requirements.txt` or `pyproject.toml`, `Dockerfile`, and module structure (`app/main.py`, `app/core/`, `app/api/`).

3. **`frontend/` Directory:**
   - Next.js application for client-side queue visualization and booking management.
   - Must maintain strict component isolation, type definitions, and standard configuration files.

4. **`infrastructure/` Directory:**
   - Holds Docker Compose files (`docker-compose.yml`, `docker-compose.override.yml`), environment templates, database initialization scripts (`postgres/init.sql`), and broker configs.
   - No application source code should reside in `infrastructure/`.

5. **`docs/` Directory:**
   - Stores system documentation, architecture diagrams, engineering kit, setup instructions, and ADRs.
   - All documentation must be written in standard GitHub-Flavored Markdown.

---

## 4. Configuration & Secret Management

- **Zero Committed Secrets:** Credentials, API keys, database passwords, JWT signing secrets, and private tokens must **never** be committed to Git.
- **Environment Templates:** Every service or root folder requiring configuration must provide a `.env.example` file detailing all supported variables with non-sensitive default placeholders.
- **Local Overrides:** Developers create a local `.env` file (which is explicitly ignored in `.gitignore`).
- **Configuration Hierarchy:** Microservice configuration must read from environment variables, falling back to local defaults only in local development profiles (`application-local.yml` or `.env`).

---

## 5. Naming & Case Conventions

To ensure consistency across polyglot components, follow these naming standards:

| Entity Type | Convention | Example |
|---|---|---|
| **Directories (Folders)** | `kebab-case` | `booking-service`, `engineering-kit` |
| **Documentation Files** | `kebab-case.md` | `03-REPOSITORY-STANDARDS.md` |
| **Java Files & Classes** | `PascalCase` | `BookingController.java`, `InventoryService.java` |
| **TypeScript / JS Files** | `camelCase` or `PascalCase` | `socketHandler.ts`, `QueueCard.tsx` |
| **Python Files** | `snake_case.py` | `rag_engine.py`, `main.py` |
| **Environment Variables** | `UPPER_SNAKE_CASE` | `POSTGRES_DB_URL`, `REDIS_HOST` |
| **Database Tables & Columns**| `snake_case` | `booking_reservations`, `created_at` |
| **REST Endpoints** | `kebab-case` (plural nouns) | `/api/v1/booking-requests`, `/api/v1/seats` |

---

## 6. Dependency Management Rules

- **Explicit Version Pinning:** All dependencies in `pom.xml`, `package.json`, and `requirements.txt` must be explicitly versioned. Unpinned wildcard dependencies (`*`, `latest`) are strictly forbidden in build files.
- **Security Audits:** Dependencies must be regularly checked for vulnerabilities using standard tooling (`npm audit`, `mvn dependency-check`, `safety`).
- **Unused Dependencies:** Prune unused libraries to minimize container image sizes and attack surfaces.

---

## 7. Related Documentation

- [Setup Guide](../setup-guide.md)
- [Engineering Notes](../engineering-notes.md)
- [Coding Standards](./04-CODING-STANDARDS.md)
- [Definition of Done](./05-DEFINITION-OF-DONE.md)
