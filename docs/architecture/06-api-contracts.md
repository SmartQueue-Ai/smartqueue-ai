# SmartQueue AI — API & Interface Contracts

**Document ID:** `ARCH-06`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

This document specifies the official API and inter-service communication contracts for SmartQueue AI. The platform enforces strict API contracts across REST endpoints, internal gRPC channels, and real-time Socket.IO WebSockets to ensure microservice independence, client predictability, and high-concurrency reliability.

All API design adheres strictly to Section 3.4 of the [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md) regarding explicit domain boundaries and system contracts.

---

## 2. REST API Specifications

SmartQueue AI microservices expose RESTful HTTP APIs for client-facing operations and external integrations. All endpoints operate over HTTPS, accept and return `application/json`, and use standard HTTP verb semantics.

### 2.1 Auth Service (`backend/auth-service`)

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user account | Public |
| `POST` | `/api/v1/auth/login` | Authenticate user and issue JWT access/refresh tokens | Public |
| `POST` | `/api/v1/auth/refresh` | Exchange valid refresh token for new access token | Public |
| `POST` | `/api/v1/auth/logout` | Revoke current refresh token & invalidate session | Authenticated |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile details | Authenticated |

### 2.2 Booking Service (`backend/booking-service`)

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/reservations` | Create a new seat reservation (requires Idempotency Key) | Authenticated |
| `GET` | `/api/v1/reservations/{id}` | Fetch reservation status and details by ID | Authenticated |
| `POST` | `/api/v1/reservations/{id}/confirm` | Confirm booking after successful payment | Authenticated |
| `DELETE` | `/api/v1/reservations/{id}/cancel` | Cancel reservation and trigger seat release | Authenticated |

### 2.3 Inventory Service (`backend/inventory-service`)

| Method | Path | Description | Access |
|---|---|---|---|
| `GET` | `/api/v1/events` | Query active events and venue listings | Public |
| `GET` | `/api/v1/events/{id}/seats` | Fetch real-time seat availability map for an event | Public |
| `POST` | `/api/v1/seats/hold` | Place temporary Redis lock hold on requested seats | Authenticated |
| `POST` | `/api/v1/seats/release` | Explicitly release temporary hold on seats | Authenticated |

### 2.4 AI Services (`ai-services/`)

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/ai/queue-estimate` | Predict estimated queue wait time for event booking | Authenticated |
| `POST` | `/api/v1/ai/assistant/chat` | Natural language booking assistant (Gemini RAG) | Authenticated |
| `POST` | `/api/v1/ai/recommend-seats` | AI-driven seat recommendation based on preferences | Authenticated |

---

## 3. Standard API Response & Error Models

All REST APIs return standardized JSON response envelopes to ensure client predictability and uniform error parsing.

### 3.1 Standard Success Envelope

```json
{
  "success": true,
  "timestamp": "2026-07-26T23:50:00Z",
  "correlationId": "sqai-trace-8f92a1b4-c3d5",
  "data": {
    "reservationId": "res_8829104",
    "status": "HELD",
    "expiresAt": "2026-07-26T23:55:00Z"
  }
}
```

### 3.2 Standard Error Payload (RFC 7807 Compliant)

```json
{
  "success": false,
  "timestamp": "2026-07-26T23:50:00Z",
  "correlationId": "sqai-trace-8f92a1b4-c3d5",
  "error": {
    "code": "INVENTORY_SEAT_LOCKED",
    "message": "The selected seat is currently held by another user",
    "status": 409,
    "path": "/api/v1/seats/hold",
    "details": [
      {
        "field": "seatId",
        "issue": "Seat 12B lock acquisition timed out"
      }
    ]
  }
}
```

---

## 4. gRPC Inter-Service Communication

For high-throughput, low-latency synchronous RPC calls between microservices (e.g., `Booking Service` verifying seat availability with `Inventory Service` before lock acquisition), SmartQueue AI uses gRPC with HTTP/2 transport and Protocol Buffers.

### 4.1 Protobuf Contract Example (`seat_service.proto`)

```protobuf
syntax = "proto3";

package com.smartqueue.inventory.v1;

option java_multiple_files = true;
option java_package = "com.smartqueue.inventory.v1";

service SeatAvailabilityService {
  rpc CheckSeatAvailability (SeatCheckRequest) returns (SeatCheckResponse);
  rpc ValidateHoldToken (HoldTokenValidationRequest) returns (HoldTokenValidationResponse);
}

message SeatCheckRequest {
  string event_id = 1;
  repeated string seat_ids = 2;
}

message SeatCheckResponse {
  string event_id = 1;
  bool all_available = 2;
  repeated SeatStatus statuses = 3;
}

message SeatStatus {
  string seat_id = 1;
  bool available = 2;
  string current_state = 3; // AVAILABLE, HELD, BOOKED
}
```

---

## 5. Real-Time WebSocket Contract (`realtime-gateway`)

The `realtime-gateway` uses Socket.IO to manage bidirectional real-time communication between client applications and the backend during flash-sale queueing and seat selection.

### 5.1 Client to Server Events

| Event Name | Payload Structure | Description |
|---|---|---|
| `queue:join` | `{ eventId: string, userId: string }` | Join event reservation queue |
| `queue:heartbeat` | `{ queueToken: string }` | Maintain active position in queue |
| `seat:subscribe` | `{ eventId: string }` | Subscribe to live seat map updates |

### 5.2 Server to Client Events

| Event Name | Payload Structure | Description |
|---|---|---|
| `queue:position_update` | `{ position: number, totalInQueue: number, estWaitSeconds: number }` | Real-time queue progress update |
| `queue:admitted` | `{ accessId: string, validUntil: string }` | User admitted from queue to booking page |
| `seat:state_changed` | `{ eventId: string, seatId: string, status: "HELD" | "BOOKED" | "AVAILABLE" }` | Live seat layout state change broadcast |
| `reservation:countdown` | `{ reservationId: string, remainingSeconds: number }` | Active hold countdown timer |

---

## 6. Related Documentation

- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
- [Microservice Boundaries](./02-microservice-boundaries.md)
- [Event Flow](./05-event-flow.md)
- [RabbitMQ Events](./09-rabbitmq-events.md)
