# SmartQueue AI — Database Design & Data Ownership

**Document ID:** `ARCH-07`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

SmartQueue AI follows the **Database-per-Service** pattern to enforce strict microservice domain boundaries. Each bounded context maintains exclusive ownership over its relational database storage. Shared or cross-microservice direct database access is strictly prohibited.

This document details database ownership, entity relationships per microservice, migration strategies, and shared data governance rules.

---

## 2. PostgreSQL Database Ownership Matrix

| Microservice | Target Database | Schema Migration Tool | Primary Entity Ownership |
|---|---|---|---|
| `auth-service` | `smartqueue_auth` | Flyway / Liquibase | Users, Credentials, Roles, Refresh Tokens |
| `booking-service` | `smartqueue_booking` | Flyway / Liquibase | Reservations, Booking Items, Payments, Idempotency Records |
| `inventory-service` | `smartqueue_inventory` | Flyway / Liquibase | Venues, Events, Seat Layouts, Seat States, Inventory Holds |

---

## 3. Microservice Schema Definitions & Tables

### 3.1 Auth Service (`smartqueue_auth`)

```text
  ┌─────────────────┐       ┌─────────────────┐
  │     users       │1     *│   user_roles    │
  ├─────────────────┤───────├─────────────────┤
  │ id (PK, UUID)   │       │ user_id (FK)    │
  │ email (UNIQUE)  │       │ role_id (FK)    │
  │ password_hash   │       └─────────────────┘
  │ status          │                │*
  │ created_at      │                │
  └─────────────────┘       ┌─────────────────┐
          │1                │      roles      │
          │                 ├─────────────────┤
          │*                │ id (PK, INT)    │
  ┌─────────────────┐       │ role_name       │
  │ refresh_tokens  │       └─────────────────┘
  ├─────────────────┤
  │ id (PK, UUID)   │
  │ user_id (FK)    │
  │ token_hash      │
  │ expires_at      │
  └─────────────────┘
```

#### Table Specifications:
- `users`: Core identity table storing user credentials and status.
- `roles`: Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`).
- `user_roles`: Join table mapping users to roles.
- `refresh_tokens`: Hashed JWT refresh tokens for session revocation.
- `audit_logs`: Security audit log recording authentication attempts and IP addresses.

---

### 3.2 Booking Service (`smartqueue_booking`)

```text
  ┌───────────────────────┐       ┌───────────────────────┐
  │     reservations      │1     *│     booking_items     │
  ├───────────────────────┤───────├───────────────────────┤
  │ id (PK, UUID)         │       │ id (PK, UUID)         │
  │ user_id (UUID)        │       │ reservation_id (FK)   │
  │ event_id (UUID)       │       │ seat_id (UUID)        │
  │ status (PENDING/...)  │       │ price_amount          │
  │ total_amount          │       └───────────────────────┘
  │ hold_expires_at       │
  │ created_at            │
  └───────────────────────┘
              │1
              │1
  ┌───────────────────────┐       ┌───────────────────────┐
  │       payments        │       │  idempotency_records  │
  ├───────────────────────┤       ├───────────────────────┤
  │ id (PK, UUID)         │       │ idempotency_key (PK)  │
  │ reservation_id (FK)   │       │ request_hash          │
  │ transaction_ref       │       │ response_payload      │
  │ status                │       │ created_at            │
  │ created_at            │       └───────────────────────┘
  └───────────────────────┘
```

#### Table Specifications:
- `reservations`: Master reservation records tracking status (`HELD`, `CONFIRMED`, `EXPIRED`, `CANCELLED`).
- `booking_items`: Individual seats attached to a reservation.
- `payments`: Payment transaction records linked to reservations.
- `idempotency_records`: Stores idempotency keys and cached API responses to guarantee zero duplicate payment or booking executions.

---

### 3.3 Inventory Service (`smartqueue_inventory`)

```text
  ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
  │     venues      │1     *│     events      │1     *│      seats      │
  ├─────────────────┤───────├─────────────────┤───────├─────────────────┤
  │ id (PK, UUID)   │       │ id (PK, UUID)   │       │ id (PK, UUID)   │
  │ name            │       │ venue_id (FK)   │       │ event_id (FK)   │
  │ location        │       │ name            │       │ section_name    │
  │ total_capacity  │       │ start_time      │       │ row_number      │
  └─────────────────┘       │ total_seats     │       │ seat_number     │
                            │ status          │       │ status (AVAILABLE│
                            └─────────────────┘       │  / HELD / BOOKED)│
                                                      │ price_tier      │
                                                      └─────────────────┘
```

#### Table Specifications:
- `venues`: Physical venue location data and capacity.
- `events`: Scheduled shows, matches, or events.
- `seats`: Detailed seat map inventory for every event.
- `seat_allocations`: Real-time allocation logs detailing seat status transitions.

---

## 4. Shared Data Rules & Cross-Service Governance

1. **Zero Direct Cross-Database Queries:** No microservice may connect to or issue SQL queries against a database owned by another microservice.
2. **No Foreign Key Constraints Across Services:** Relationships across microservice boundaries (e.g., `reservations.user_id` referencing `users.id`) are enforced logically at the application level; physical SQL `FOREIGN KEY` constraints across microservice databases are strictly prohibited.
3. **Data Replication via Event Sourcing:** When a service requires information owned by another context (e.g., `Booking Service` displaying user email or event title), it must consume domain events published over RabbitMQ and maintain a read-optimized local projection.
4. **Transactional Idempotency:** All state-modifying database transactions must record an idempotency key to prevent double-processing on retries.

---

## 5. Related Documentation

- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
- [Microservice Boundaries](./02-microservice-boundaries.md)
- [API Contracts](./06-api-contracts.md)
- [Redis Strategy](./08-redis-strategy.md)
