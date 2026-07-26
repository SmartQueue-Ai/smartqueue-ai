# SmartQueue AI — Synchronous Request Execution Flow

**Document ID:** `ARCH-04`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

This document specifies the synchronous execution pathways for client requests entering SmartQueue AI. It details the end-to-end traversal from client applications through gateway edge layers, domain microservices, and infrastructure datastores back to the client.

```text
[ Frontend Client ] ──(HTTP/REST)──► [ API Gateway ] ──(REST/RPC)──► [ Microservices ] ──(SQL/Redis)──► [ Datastores ]
                                                                                                            │
[ Client UI ] ◄──(HTTP Response 200/409)────────────────────────────────────────────────────────────────────┘
```

---

## 2. High-Concurrency Seat Reservation Lock Flow

This request path represents the most critical hot path in SmartQueue AI: a user attempting to reserve seats during a flash sale.

### 2.1 Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Client as Frontend Client (Next.js)
    participant Gateway as API Gateway
    participant Auth as Auth Service
    participant Booking as Booking Service
    participant Inventory as Inventory Service
    participant Redis as Redis Cache (Locks)
    participant DB as PostgreSQL DB

    Client->>Gateway: POST /api/v1/bookings<br/>Header: Authorization: Bearer <JWT><br/>Body: { eventId, seatIds: ["A101", "A102"] }
    
    Gateway->>Auth: Validate JWT Access Token
    Auth-->>Gateway: 200 OK (userId: "usr_9981", valid: true)
    
    Gateway->>Booking: Forward Request (X-Correlation-ID: "req-7782", userId: "usr_9981")
    
    Booking->>Inventory: POST /api/v1/inventory/lock-seats<br/>Body: { userId, eventId, seatIds: ["A101", "A102"] }
    
    Inventory->>Redis: SETNX smartqueue:inventory:seat-lock:evt_1:A101 "usr_9981" EX 600<br/>SETNX smartqueue:inventory:seat-lock:evt_1:A102 "usr_9981" EX 600
    
    alt All Locks Acquired Successfully
        Redis-->>Inventory: OK (Keys Created)
        Inventory-->>Booking: 200 OK { lockAcquired: true, lockExpiresAt: 1770000600 }
        
        Booking->>DB: INSERT INTO bookings (id, user_id, status, expires_at)<br/>VALUES ('bk_4410', 'usr_9981', 'LOCK_ACQUIRED', ...)
        DB-->>Booking: 1 Row Inserted
        
        Booking-->>Gateway: 201 Created { bookingId: "bk_4410", status: "LOCK_ACQUIRED", expiresAt: 600 }
        Gateway-->>Client: 201 Created (JSON Payload + Correlation Headers)
    else Lock Failed (Seat Already Held)
        Redis-->>Inventory: FAIL (Key A101 Exists)
        Inventory->>Redis: DEL smartqueue:inventory:seat-lock:evt_1:A102 (Rollback partial locks)
        Inventory-->>Booking: 409 Conflict { error: "SEAT_ALREADY_LOCKED", seatId: "A101" }
        Booking-->>Gateway: 409 Conflict { message: "Seat A101 is no longer available" }
        Gateway-->>Client: 409 Conflict (User prompted to select alternative seats)
    end
```

### 2.2 Detailed Step-by-Step Breakdown

1. **Request Ingestion:** The Next.js frontend sends an HTTP `POST` request to `/api/v1/bookings` with an `Authorization: Bearer <JWT>` header and JSON body containing `eventId` and selected `seatIds`.
2. **Gateway Authentication Check:** The API Gateway intercepts the request, extracts the JWT, and verifies its cryptographic signature against the Auth Service public key.
3. **Correlation ID Injection:** The Gateway generates a unique `X-Correlation-ID` header (`req-7782`) and appends `X-User-Id: usr_9981` to the downstream request context.
4. **Lock Acquisition Request:** The Booking Service calls the Inventory Service via internal HTTP/REST to lock the requested seats.
5. **Atomic Redis Lock Execution:** The Inventory Service issues an atomic Redis multi-key `SETNX` command with a 600-second TTL.
   - *Success Branch:* If all keys are successfully set, Redis returns `OK`. The Inventory Service returns HTTP 200 to Booking Service.
   - *Failure Branch (Race Condition):* If any requested seat is already locked by another user, Redis returns failure. The Inventory Service immediately releases any partial locks acquired during the attempt, preventing dangling locks, and returns HTTP 409 Conflict.
6. **Booking Persistence:** Upon successful lock acquisition, the Booking Service writes a record in `bookings` table with status `LOCK_ACQUIRED` and sets an expiration timestamp.
7. **Client Response:** The Gateway returns HTTP 201 Created to the client with booking ID, lock expiration timer, and correlation metadata.

---

## 3. User Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant Gateway as API Gateway
    participant Auth as Auth Service
    participant DB as PostgreSQL (Users)
    participant Redis as Redis (Sessions)

    Client->>Gateway: POST /api/v1/auth/login<br/>Body: { email, password }
    Gateway->>Auth: Forward Login Request
    Auth->>DB: SELECT * FROM users WHERE email = ?
    DB-->>Auth: User Record (password_hash: "$2a$10$...")
    
    Auth->>Auth: Verify BCrypt Password Match
    alt Invalid Credentials
        Auth-->>Gateway: 401 Unauthorized { error: "INVALID_CREDENTIALS" }
        Gateway-->>Client: 401 Unauthorized
    else Valid Credentials
        Auth->>Auth: Generate RS256 Access Token (15m TTL)<br/>Generate Refresh Token (7d TTL)
        Auth->>Redis: SET smartqueue:auth:session:usr_9981 refreshToken EX 604800
        Auth-->>Gateway: 200 OK { accessToken, refreshToken, tokenType: "Bearer" }
        Gateway-->>Client: 200 OK (Set Secure HTTP-Only Cookies / JSON)
    end
```

---

## 4. Related Documentation

- [System Architecture Overview](./01-system-overview.md)
- [Microservice Boundaries](./02-microservice-boundaries.md)
- [Service Responsibilities](./03-service-responsibilities.md)
- [Event Flow](./05-event-flow.md)
- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
