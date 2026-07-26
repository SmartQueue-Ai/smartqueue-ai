# SmartQueue AI — Service Responsibilities & Domain Invariants

**Document ID:** `ARCH-03`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

This document provides a granular breakdown of domain models, business invariants, API contracts, and message specifications owned by each microservice in SmartQueue AI.

---

## 2. Granular Service Responsibilities

### 2.1 Auth Service (`backend/auth-service/`)

#### Domain Models Owned
- `User`: Core user record (user_id, email, password_hash, status, created_at).
- `Role`: Security authority roles (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_OPERATOR`).
- `UserRole`: Mapping table assigning authorities to users.
- `RefreshToken`: Cryptographic refresh tokens for session renewal.

#### API Endpoints Owned
- `POST /api/v1/auth/register` — Create user account.
- `POST /api/v1/auth/login` — Authenticate credentials and return JWT pair (Access + Refresh).
- `POST /api/v1/auth/refresh` — Issue new Access Token using valid Refresh Token.
- `POST /api/v1/auth/logout` — Revoke active token and clear session state.
- `GET /api/v1/auth/me` — Retrieve current authenticated user profile.

#### Business Invariants & Rules Enforced
1. User emails must be unique and validated before account creation.
2. Passwords must be hashed using BCrypt (work factor >= 10); raw passwords must never be logged or stored.
3. Access JWT tokens must expire after a maximum of 15 minutes.
4. Revoked tokens must be blacklisted in Redis until their natural expiration time.

---

### 2.2 Booking Service (`backend/booking-service/`)

#### Domain Models Owned
- `Booking`: Primary reservation record (booking_id, user_id, event_id, status, total_amount, expiration_time, created_at).
- `BookingItem`: Individual ticket items attached to a booking (item_id, booking_id, seat_id, price).
- `BookingState`: Finite State Machine enum (`INITIATED`, `LOCK_ACQUIRED`, `PAYMENT_PENDING`, `CONFIRMED`, `EXPIRED`, `CANCELLED`).
- `PaymentAuditLog`: Ledger tracking payment authorization callbacks and transaction references.

#### API Endpoints Owned
- `POST /api/v1/bookings` — Initiate new booking reservation workflow.
- `GET /api/v1/bookings/{id}` — Retrieve booking details and current state.
- `POST /api/v1/bookings/{id}/confirm` — Complete booking upon payment confirmation.
- `POST /api/v1/bookings/{id}/cancel` — Abort booking and release held inventory.
- `GET /api/v1/bookings/user/{userId}` — List user booking history.

#### Business Invariants & Rules Enforced
1. A user cannot initiate more than 1 active pending booking simultaneously during high-concurrency windows.
2. A single booking transaction cannot exceed 4 seats/items.
3. A booking in `LOCK_ACQUIRED` state automatically transitions to `EXPIRED` if payment is not confirmed within 600 seconds (10 minutes).
4. Outbound events (`booking.created`, `booking.confirmed`, `booking.expired`) must be transactionally committed with state changes (Outbox pattern).

---

### 2.3 Inventory Service (`backend/inventory-service/`)

#### Domain Models Owned
- `Venue`: Physical or virtual event location metadata (venue_id, name, capacity, layout_config).
- `Event`: Scheduled performance or match instance (event_id, venue_id, start_time, sales_window_status).
- `Seat`: Specific resource unit (seat_id, venue_id, section, row, seat_number, base_price).
- `SeatLock`: Redis-backed transient lock model (lock_key, seat_id, user_id, acquired_at, ttl_seconds).

#### API Endpoints Owned
- `GET /api/v1/events/{id}/seats` — Fetch current seat availability map for an event.
- `POST /api/v1/inventory/lock-seats` — Attempt atomic temporary lock on requested seat list.
- `POST /api/v1/inventory/release-seats` — Explicitly release held seat locks.
- `POST /api/v1/inventory/confirm-seats` — Permanently mark seats as `SOLD` upon booking completion.

#### Business Invariants & Rules Enforced
1. Zero double-booking: A seat can be held by at most ONE active lock key in Redis at any point in time.
2. Seat lock attempts must be executed atomically across all requested seats in a single request (all-or-nothing allocation).
3. If any seat in a multi-seat request is unavailable, all temporary locks in that attempt must be immediately rolled back.
4. Redis lock keys must enforce a strict TTL (default 600s) to prevent dangling lock deadlocks if a client drops off.

---

### 2.4 Realtime Gateway (`backend/realtime-gateway/`)

#### Domain Models Owned
- `ClientSession`: Active WebSocket connection state (socket_id, user_id, connected_at, room_subscriptions).
- `QueueRoom`: Virtual room grouping users waiting in queue for a specific event window.

#### Protocols & Events Managed
- **WebSocket Handshake:** Authenticates JWT token via query parameter or header before upgrading HTTP to WS.
- **Inbound Client Events:** `join:queue`, `leave:queue`, `ping`.
- **Outbound Server Pushes:**
  - `queue:update` — Current position in line and estimated wait time.
  - `seat:status_change` — Live update when seats are locked or released by others.
  - `booking:status` — Instant confirmation or expiration notification.

#### Business Invariants & Rules Enforced
1. Unauthenticated WebSocket connections must be closed immediately during handshake.
2. Connection heartbeats (ping/pong) must disconnect stale clients after 30 seconds of inactivity.
3. Queue position updates must be rate-limited to avoid client socket flooding (maximum 1 push per second per client).

---

### 2.5 AI Services (`ai-services/`)

#### Domain Models & Indices Owned
- `VectorIndex`: ChromaDB vector database storing event FAQs, venue rules, and booking policies.
- `PredictiveModelContext`: Real-time queue metrics buffer for computing dynamic wait-time estimations.

#### API Endpoints Owned
- `POST /api/v1/ai/assistant/chat` — Natural language booking query & RAG assistant.
- `GET /api/v1/ai/predictions/queue-wait-time` — Predict queue wait duration based on current throughput metrics.
- `POST /api/v1/ai/recommendations/events` — Personalized event recommendation engine.

#### Business Invariants & Rules Enforced
1. LLM responses must strictly cite vector database context to prevent hallucinations regarding pricing or seat policies.
2. AI endpoints must fail gracefully if external LLM APIs (Gemini) experience rate limiting or outages.

---

## 3. Related Documentation

- [System Architecture Overview](./01-system-overview.md)
- [Microservice Boundaries](./02-microservice-boundaries.md)
- [Request Flow](./04-request-flow.md)
- [Event Flow](./05-event-flow.md)
