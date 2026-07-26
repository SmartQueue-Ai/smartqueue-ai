# SmartQueue AI — Microservice Boundaries & Service Contracts

**Document ID:** `ARCH-02`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

SmartQueue AI follows a domain-driven microservice architecture. Each service operates as an autonomous bounded context with strict ownership over its code, datastore schemas, and API contracts. Direct cross-microservice database access is strictly prohibited.

---

## 2. Microservice Inventory & Boundaries

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SMARTQUEUE AI MONOREPO                             │
├─────────────────┬──────────────────┬─────────────────┬──────────────────────┤
│ Microservice    │ Technology Stack │ Primary Domain  │ Primary Storage      │
├─────────────────┼──────────────────┼─────────────────┼──────────────────────┤
│ Auth Service    │ Java 21 / Spring │ Security, RBAC, │ PostgreSQL (`auth`)   │
│                 │ Boot 3           │ User Identities │ Redis (Sessions)     │
├─────────────────┼──────────────────┼─────────────────┼──────────────────────┤
│ Booking Service │ Java 21 / Spring │ Reservations,   │ PostgreSQL (`booking`)│
│                 │ Boot 3           │ Saga Lifecycle  │ RabbitMQ (Events)    │
├─────────────────┼──────────────────┼─────────────────┼──────────────────────┤
│ Inventory Svc   │ Java 21 / Spring │ Seats, Locks,   │ PostgreSQL (`invent`)│
│                 │ Boot 3           │ Availability    │ Redis (`SETNX` Locks)│
├─────────────────┼──────────────────┼─────────────────┼──────────────────────┤
│ Realtime GW     │ Node.js 20 / TS  │ WebSockets,     │ Redis (PubSub/State) │
│                 │ Socket.IO        │ Live Pushes     │ RabbitMQ Consumer    │
├─────────────────┼──────────────────┼─────────────────┼──────────────────────┤
│ AI Services     │ Python 3.11 /    │ RAG, Semantic   │ ChromaDB Vector Store│
│                 │ FastAPI          │ Predictions     │ Gemini API Gateway   │
└─────────────────┴──────────────────┴─────────────────┴──────────────────────┘
```

---

## 3. Microservice Specifications

### 3.1 Auth Service (`backend/auth-service/`)

- **Ownership Boundary:** User accounts, credentials, JWT generation/validation, role-based access control (RBAC), and active session state.
- **Primary Responsibilities:**
  - Authenticate users via username/password or OAuth.
  - Issue cryptographically signed RS256 JWT tokens containing user claims and roles.
  - Validate access tokens for API Gateway and downstream microservices.
  - Manage user registration, password hashing (BCrypt), and profile management.
- **Communication Protocols:**
  - **Inbound:** Synchronous REST HTTP (`/api/v1/auth/*`).
  - **Outbound:** Asynchronous RabbitMQ events (`user.created`, `user.locked`).
- **Dependencies:** PostgreSQL (Users schema), Redis (Revoked token blacklist & active sessions).
- **Data Isolation:** Exclusive owner of PostgreSQL `users`, `roles`, and `user_roles` tables. No other microservice may access these tables directly.

---

### 3.2 Booking Service (`backend/booking-service/`)

- **Ownership Boundary:** Booking lifecycle orchestration, reservation state machine, confirmation workflows, and payment event correlation.
- **Primary Responsibilities:**
  - Orchestrate reservation creation (`PENDING` -> `CONFIRMED` / `EXPIRED` / `CANCELLED`).
  - Coordinate seat lock acquisition with the Inventory Service.
  - Enforce booking business rules (e.g., maximum 4 tickets per user per transaction).
  - Publish reservation lifecycle events to RabbitMQ.
- **Communication Protocols:**
  - **Inbound:** Synchronous REST HTTP (`/api/v1/bookings/*`).
  - **Outbound:** Synchronous REST calls to Inventory Service (Internal HTTP/gRPC); Asynchronous RabbitMQ events (`booking.created`, `booking.confirmed`, `booking.expired`).
- **Dependencies:** PostgreSQL (Bookings schema), Inventory Service API, RabbitMQ Broker.
- **Data Isolation:** Exclusive owner of PostgreSQL `bookings`, `booking_items`, and `payment_audit_logs` tables.

---

### 3.3 Inventory Service (`backend/inventory-service/`)

- **Ownership Boundary:** Real-time seat allocation, inventory layout maps, temporary seat locks, and physical availability state.
- **Primary Responsibilities:**
  - Manage venue seat maps, event schedules, and resource availability counters.
  - Execute atomic temporary seat locks (e.g., hold seat A-12 for 10 minutes) using Redis `SETNX` primitives.
  - Prevent double-booking under extreme concurrent request spikes.
  - Confirm permanent seat allocation upon successful booking completion.
- **Communication Protocols:**
  - **Inbound:** Synchronous REST HTTP / gRPC (`/api/v1/inventory/*`, `/api/v1/seats/*`).
  - **Outbound:** Asynchronous RabbitMQ events (`inventory.locked`, `inventory.released`, `inventory.depleted`).
- **Dependencies:** Redis (In-memory distributed locks), PostgreSQL (Inventory & Seat state).
- **Data Isolation:** Exclusive owner of PostgreSQL `events`, `venues`, `seat_maps`, and `seats` tables, as well as Redis key namespace `smartqueue:inventory:*`.

---

### 3.4 Realtime Gateway (`backend/realtime-gateway/`)

- **Ownership Boundary:** Client WebSocket persistent connections, Socket.IO room management, queue progress broadcasts, and live notification pushes.
- **Primary Responsibilities:**
  - Maintain thousands of persistent WebSocket client connections.
  - Authenticate client WebSocket handshake requests using JWT tokens from Auth Service.
  - Subscribe to RabbitMQ exchanges and broadcast live state updates (queue position, seat hold countdowns, booking results) to connected client sockets.
  - Manage Socket.IO rooms per event or booking session.
- **Communication Protocols:**
  - **Inbound:** WebSockets / Socket.IO from Frontend Clients; Asynchronous RabbitMQ AMQP event consumer.
  - **Outbound:** WebSockets push frames to clients.
- **Dependencies:** RabbitMQ Broker (Consumer), Redis (Socket connection state & room registry).
- **Data Isolation:** Exclusive owner of Redis key namespace `smartqueue:realtime:*`. Does not own relational database schemas.

---

### 3.5 AI Services (`ai-services/`)

- **Ownership Boundary:** Natural language booking assistance, semantic venue/event search, predictive queue wait time calculations, and RAG pipelines.
- **Primary Responsibilities:**
  - Expose FastAPI endpoints for natural language user queries and recommendation generation.
  - Maintain a vector index (ChromaDB) of venue layouts, booking policies, and FAQs.
  - Process real-time system metrics from RabbitMQ to predict queue drain rates and estimated wait times.
  - Integrate with Gemini API / LangChain for intelligent conversational booking flows.
- **Communication Protocols:**
  - **Inbound:** Synchronous REST HTTP (`/api/v1/ai/*`); Asynchronous RabbitMQ event listener (`booking.*`, `queue.*`).
  - **Outbound:** External HTTP calls to Gemini API.
- **Dependencies:** ChromaDB (Vector store), Gemini API, RabbitMQ Broker.
- **Data Isolation:** Exclusive owner of vector index stores and local AI model caches.

---

## 4. Cross-Microservice Communication Matrix

| Source Service | Target Service | Interaction Type | Protocol | Purpose |
|---|---|---|---|---|
| **API Gateway** | **Auth Service** | Synchronous | REST / HTTP | Validate JWT access tokens on inbound requests |
| **API Gateway** | **Booking Service** | Synchronous | REST / HTTP | Proxy user booking creation requests |
| **Booking Service** | **Inventory Service** | Synchronous | REST / HTTP | Request atomic temporary seat lock |
| **Booking Service** | **RabbitMQ** | Asynchronous | AMQP (`topic`) | Publish `booking.created` / `booking.confirmed` |
| **Inventory Service** | **Redis** | Synchronous | Redis Protocol | Execute `SETNX` distributed seat lock |
| **RabbitMQ** | **Realtime Gateway** | Asynchronous | AMQP (`fanout/topic`) | Push live updates to client WebSockets |
| **RabbitMQ** | **AI Services** | Asynchronous | AMQP (`topic`) | Ingest events for RAG & predictive analytics |

---

## 5. Related Documentation

- [System Architecture Overview](./01-system-overview.md)
- [Service Responsibilities](./03-service-responsibilities.md)
- [Request Flow](./04-request-flow.md)
- [Event Flow](./05-event-flow.md)
- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
