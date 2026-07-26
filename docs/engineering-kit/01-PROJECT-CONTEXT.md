# SmartQueue AI — Project Context & Domain Vision

**Document ID:** `EK-01`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Engineering Team  

---

## 1. Executive Summary

SmartQueue AI is a high-performance, distributed reservation infrastructure platform engineered to solve real-world problems in high-concurrency booking environments. It simulates production-grade infrastructure under extreme traffic spikes similar to:

- **IRCTC Tatkal** (Railway ticket reservations during opening windows)
- **Ticketmaster / Live Nation** (Concert & event ticketing flash sales)
- **redBus** (Intercity bus seat locking and allocation)
- **BookMyShow** (Movie & entertainment venue booking)

The system focuses heavily on distributed systems engineering, queue management, distributed locking, microservices coordination, real-time WebSockets synchronization, and AI-assisted reservation intelligence.

---

## 2. Problem Domain & Engineering Challenges

High-concurrency reservation systems face unique architectural challenges:

1. **Thundering Herd & Traffic Spikes:** Millions of incoming HTTP/WebSocket requests within seconds when a sales window opens.
2. **Race Conditions & Double-Booking:** Multiple concurrent workers attempting to reserve the exact same seat or inventory item simultaneously.
3. **Queue Backpressure & Flow Control:** Preventing downstream relational databases from being overwhelmed by decoupling request ingestion from reservation processing.
4. **Realtime State Synchronization:** Providing sub-second updates to clients regarding queue placement, seat status changes, and reservation expiration timeouts.
5. **Distributed Transaction Resilience:** Managing multi-step workflows (reserve inventory -> process payment -> issue ticket -> notify user) across isolated microservices without data inconsistency.

---

## 3. High-Level Architecture & Components

SmartQueue AI uses a monorepo microservice architecture organized as follows:

```text
                       [ Frontend (Next.js) ]
                                 │
                     (HTTP / WebSockets / Socket.IO)
                                 │
                   ┌─────────────┴─────────────┐
                   ▼                           ▼
       [ Realtime Gateway ]          [ API Gateway / Services ]
       (Node.js / Socket.IO)         (Spring Boot Microservices)
                   │                           │
                   │        ┌──────────────────┼──────────────────┐
                   │        ▼                  ▼                  ▼
                   │   [ Auth Service ] [ Booking Service ] [ Inventory Service ]
                   │        │                  │                  │
                   └────────┼──────────────────┼──────────────────┘
                            │                  │
                            ▼                  ▼
                ┌─────────────────────────────────────────┐
                │          Infrastructure Layer           │
                │ ┌────────────┐ ┌───────┐ ┌────────────┐ │
                │ │ PostgreSQL │ │ Redis │ │ RabbitMQ   │ │
                │ └────────────┘ └───────┘ └────────────┘ │
                └─────────────────────────────────────────┘
                                     ▲
                                     │
                           [ AI Services (FastAPI) ]
                           (LangChain / Gemini RAG)
```

### Core Microservices

- **Auth Service (Spring Boot):** Manages user identity, JWT issuance, token verification, and Role-Based Access Control (RBAC).
- **Booking Service (Spring Boot):** Orchestrates the booking workflow lifecycle, validation, reservation state machine, and confirmation events.
- **Inventory Service (Spring Boot):** Controls seat/resource availability, temporary locks, inventory seat maps, and atomic allocation.
- **Realtime Gateway (Node.js / Socket.IO):** Manages persistent WebSocket connections, queue position pushes, seat hold updates, and live notifications.
- **AI Services (Python / FastAPI):** Powers intelligent booking assistance, semantic search, queue status predictions, and Retrieval-Augmented Generation (RAG) capabilities.

### Infrastructure Layer

- **PostgreSQL:** Primary transactional datastore for relational integrity (users, bookings, seat state, payment logs).
- **Redis:** In-memory store for high-speed distributed locking (`SETNX`), rate limiting, active session storage, and queue management.
- **RabbitMQ:** Asynchronous message broker handling inter-service event publishing, booking confirmation pipelines, and retry dead-letter queues (DLQs).

---

## 4. Repository Structure & Workspace Layout

SmartQueue AI operates as a structured monorepo:

```text
smartqueue-ai/
├── backend/                # Spring Boot Microservices & Node.js Realtime Gateway
│   ├── auth-service/       # Authentication & Authorization microservice
│   ├── booking-service/    # Booking orchestration microservice
│   ├── inventory-service/  # Inventory allocation & locking microservice
│   └── realtime-gateway/   # Socket.IO WebSocket gateway
├── ai-services/            # Python FastAPI AI & RAG service modules
├── frontend/               # Next.js web application interface
├── infrastructure/         # Docker Compose, Kubernetes manifests & DevOps configs
├── docs/                   # Platform architecture, setup guides, and engineering kit
│   └── engineering-kit/    # Core engineering standards and constitution
└── scripts/                # Development, seeding, and setup scripts
```

---

## 5. Performance & Scalability Target Goals

- **Concurrency:** Support high peak request rates with queue backpressure protection.
- **Lock Acquisition:** Sub-10ms seat lock verification using Redis in-memory atomic primitives.
- **Zero Double-Bookings:** 100% deterministic inventory isolation under synthetic stress testing.
- **System Observability:** Full request correlation across HTTP, RabbitMQ, and database calls using tracing headers.

---

## 6. Related Documentation

- [Architecture Overview](../architecture.md)
- [Engineering Notes](../engineering-notes.md)
- [Setup Guide](../setup-guide.md)
- [Engineering Constitution](./00-ENGINEERING-CONSTITUTION.md)
- [Engineering Principles](./02-ENGINEERING-PRINCIPLES.md)
