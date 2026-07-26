# SmartQueue AI — RabbitMQ Event Bus Architecture

**Document ID:** `ARCH-09`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

RabbitMQ serves as the asynchronous event bus for SmartQueue AI. It decouples microservice bounded contexts, handles background processing tasks, manages peak queue backpressure, and guarantees eventual consistency across microservices.

All event messaging adheres to Section 3.4 of the [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md) regarding event-driven asynchronous communication and strict contract schemas.

---

## 2. RabbitMQ Exchange Topology

SmartQueue AI utilizes dedicated Topic Exchanges (`topic`) to route messages dynamically based on wildcard routing keys.

| Exchange Name | Type | Purpose |
|---|---|---|
| `smartqueue.domain.events` | `topic` | Primary exchange for core business domain events (reservations, seats, payments) |
| `smartqueue.notifications.exchange` | `topic` | Exchange for user notification events (emails, SMS alerts, socket pushes) |
| `smartqueue.deadletter.exchange` | `topic` | Dead-Letter Exchange (DLX) for unprocessable or failed messages |

---

## 3. Queue Topology & Binding Matrix

```text
                                  ┌───────────────────────────────┐
                                  │   smartqueue.domain.events    │
                                  │       (Topic Exchange)        │
                                  └───────────────────────────────┘
                                     │            │            │
             "reservation.*"         │            │            │ "payment.*"
     ┌───────────────────────────────┘            │            └──────────────────────────────┐
     │                                            │ "seat.*"                                  │
     ▼                                            ▼                                           ▼
┌───────────────────────────────┐    ┌───────────────────────────────┐           ┌───────────────────────────────┐
│ booking.reservation.queue     │    │ inventory.seat.queue          │           │ notification.payment.queue    │
└───────────────────────────────┘    └───────────────────────────────┘           └───────────────────────────────┘
```

| Queue Name | Bound Exchange | Routing Key Pattern | Consumer Service |
|---|---|---|---|
| `booking.reservation.queue` | `smartqueue.domain.events` | `reservation.*` | `booking-service` |
| `inventory.seat.queue` | `smartqueue.domain.events` | `seat.*` | `inventory-service` |
| `realtime.broadcast.queue` | `smartqueue.domain.events` | `*.state_changed` | `realtime-gateway` |
| `notification.user.queue` | `smartqueue.notifications.exchange` | `notification.*` | `notification-service` |
| `ai.analytics.queue` | `smartqueue.domain.events` | `reservation.#`, `seat.#` | `ai-services` |
| `dlq.failed.events.queue` | `smartqueue.deadletter.exchange` | `dlq.#` | Dead-Letter Processor / Admin Dashboard |

---

## 4. Key Event Payload Contracts

All domain events published to RabbitMQ must follow a standardized JSON envelope structure containing metadata and payload blocks.

### 4.1 `ReservationCreatedEvent`

- **Exchange:** `smartqueue.domain.events`
- **Routing Key:** `reservation.created`
- **Producer:** `booking-service`
- **Consumers:** `inventory-service`, `realtime-gateway`, `ai-services`

```json
{
  "eventId": "evt_d92f8a1c-30b4-4c81",
  "eventType": "ReservationCreatedEvent",
  "timestamp": "2026-07-26T23:51:00Z",
  "correlationId": "sqai-trace-8f92a1b4-c3d5",
  "producer": "booking-service",
  "payload": {
    "reservationId": "res_8829104",
    "userId": "usr_501",
    "eventId": "evt_101",
    "seatIds": ["seat_12B", "seat_12C"],
    "totalAmount": 150.00,
    "holdExpiresAt": "2026-07-26T23:56:00Z"
  }
}
```

### 4.2 `SeatLockedEvent`

- **Exchange:** `smartqueue.domain.events`
- **Routing Key:** `seat.locked`
- **Producer:** `inventory-service`
- **Consumers:** `booking-service`, `realtime-gateway`

```json
{
  "eventId": "evt_e71a042d-91f2",
  "eventType": "SeatLockedEvent",
  "timestamp": "2026-07-26T23:51:01Z",
  "correlationId": "sqai-trace-8f92a1b4-c3d5",
  "producer": "inventory-service",
  "payload": {
    "eventId": "evt_101",
    "seatId": "seat_12B",
    "lockedByUserId": "usr_501",
    "ttlSeconds": 15
  }
}
```

### 4.3 `PaymentCompletedEvent`

- **Exchange:** `smartqueue.domain.events`
- **Routing Key:** `payment.completed`
- **Producer:** `booking-service`
- **Consumers:** `inventory-service`, `notification-service`

```json
{
  "eventId": "evt_a109823f-4421",
  "eventType": "PaymentCompletedEvent",
  "timestamp": "2026-07-26T23:53:00Z",
  "correlationId": "sqai-trace-8f92a1b4-c3d5",
  "producer": "booking-service",
  "payload": {
    "reservationId": "res_8829104",
    "transactionRef": "tx_994821039",
    "amountPaid": 150.00,
    "status": "SUCCESS"
  }
}
```

---

## 5. Dead-Letter Queue (DLQ) & Retry Policy

To guarantee zero message loss during temporary consumer failures or network partitions:

1. **Dead-Letter Exchange Binding:** Every operational queue is configured with `x-dead-letter-exchange: smartqueue.deadletter.exchange` and `x-dead-letter-routing-key: dlq.failed`.
2. **Exponential Backoff Retries:** Failed message processing triggers up to **3 retry attempts** with exponential backoff delays (1s, 5s, 25s).
3. **DLQ Escalation:** Messages failing after 3 retries are dead-lettered to `dlq.failed.events.queue` for administrative inspection and manual replay.
4. **Consumer Idempotency:** Consumers must track processed `eventId` values in Redis or PostgreSQL to safely discard duplicate message deliveries (at-least-once delivery guarantee).

---

## 6. Related Documentation

- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
- [Event Flow](./05-event-flow.md)
- [API Contracts](./06-api-contracts.md)
- [Redis Strategy](./08-redis-strategy.md)
