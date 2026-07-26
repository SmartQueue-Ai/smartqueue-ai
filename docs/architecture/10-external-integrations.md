# SmartQueue AI — External AI & Service Integrations

**Document ID:** `ARCH-10`  
**Version:** `1.0.0`  
**Status:** `Active`  
**Owner:** SmartQueue AI Architectural Board  

---

## 1. Overview

SmartQueue AI integrates advanced Artificial Intelligence capability into distributed queue management. Using Google Gemini LLM, LangChain orchestration, and ChromaDB vector search, the platform delivers intelligent wait-time prediction, semantic event discovery, and conversational booking assistance.

This document details the architecture, integration patterns, security boundaries, and fallback strategies for all external AI models, vector stores, and third-party services.

---

## 2. Google Gemini LLM Integration

SmartQueue AI utilizes the **Google Gemini API** (via `google-genai` Python SDK) within `ai-services/` to power natural language interaction and intelligent seat recommendation reasoning.

```text
┌─────────────────┐      HTTP REST      ┌──────────────────┐     Gen AI SDK     ┌─────────────────┐
│ Client App / UI │────────────────────>│   ai-services    │───────────────────>│  Google Gemini  │
│                 │<────────────────────│ (FastAPI App)    │<───────────────────│    API Model    │
└─────────────────┘                     └──────────────────┘                    └─────────────────┘
                                                  │
                                         (Retrieve Context)
                                                  ▼
                                        ┌──────────────────┐
                                        │ ChromaDB Store   │
                                        └──────────────────┘
```

### 2.1 Primary Use Cases
- **Conversational Booking Assistant:** Interprets complex user booking prompts (e.g., *"Find 2 aisle seats under $80 near the front for Saturday's concert"*).
- **Intelligent Wait-Time Reasoning:** Generates human-readable queue status summaries explaining current processing velocity and estimated admission time.

### 2.2 Security & Rate Limiting Controls
- **Zero Hardcoded API Keys:** Gemini API keys are injected exclusively via environment variables (`GEMINI_API_KEY`) and managed via secure secret storage.
- **Rate Limit Resilience:** API calls implement Token Bucket rate limiting and exponential backoff retry handlers to prevent HTTP 429 quota exhaustion.
- **Sanitized Prompts:** User inputs are sanitized prior to prompt construction to prevent prompt injection attacks.

---

## 3. LangChain Orchestration Framework

LangChain serves as the core Python orchestration layer in `ai-services/` for structuring multi-step LLM chains, agent memory, and Retrieval-Augmented Generation (RAG) pipelines.

### 3.1 LangChain Architecture Components

```text
+-------------------------------------------------------------------+
|                        LangChain Pipeline                         |
+----------------------------------+--------------------------------+
| Prompt Templates                 | Event & Seat Context Formitted |
| Retrieval Chain (RAG)            | ChromaDB Similarity Search     |
| Output Parsers                   | Pydantic Schema Extraction     |
| Conversation Memory              | Redis Session Backed           |
+----------------------------------+--------------------------------+
```

1. **Prompt Templates:** Standardized templates enforcing system instructions, output schemas, and domain guardrails.
2. **Retrieval Chains (RAG):** Combines vector similarity search from ChromaDB with Gemini model generation to answer domain queries accurately without hallucination.
3. **Structured Output Parsers:** Uses Pydantic schema parsers to guarantee Gemini returns valid JSON matching backend contract schemas.

---

## 4. ChromaDB Vector Store Architecture

ChromaDB operates as an embedded vector database in `ai-services/` to store and query high-dimensional embeddings of event metadata, venue maps, and platform documentation.

### 4.1 Vector Collections & Indexing

| Collection Name | Embeddings Model | Stored Data | Query Pattern |
|---|---|---|---|
| `event_catalog_vector` | Google `text-embedding-004` | Event descriptions, categories, performers, seating guidelines | Semantic event search & natural language filtering |
| `queue_faqs_vector` | Google `text-embedding-004` | Platform support documentation, queue policies, booking rules | Automated customer assistance & troubleshooting |

### 4.2 Indexing & Synchronization Lifecycle
- **Sync Trigger:** When new events are published or updated in `inventory-service`, a domain event (`event.created`) is published via RabbitMQ.
- **Vector Pipeline:** `ai-services` consumes the event, generates text embeddings using Google `text-embedding-004`, and upserts the vector document into ChromaDB.

---

## 5. Future External Integrations

To maintain microservice decoupling and extensibility, future external integrations will follow standard interface adapters:

```text
                                  ┌───────────────────────────────┐
                                  │   Third-Party Integrations    │
                                  └───────────────────────────────┘
                                     │            │            │
                                     │            │            │
             ┌───────────────────────┘            │            └───────────────────────┐
             ▼                                    ▼                                    ▼
┌───────────────────────────────┐   ┌───────────────────────────────┐    ┌───────────────────────────────┐
│ Payment Gateways              │   │ Notification Providers        │    │ Identity Providers (OAuth2)   │
│ - Stripe API                  │   │ - SendGrid (Email API)        │    │ - Google OAuth2               │
│ - Razorpay Webhooks           │   │ - Twilio (SMS API)            │    │ - GitHub SSO                  │
└───────────────────────────────┘   └───────────────────────────────┘    └───────────────────────────────┘
```

1. **Payment Gateways (Stripe / Razorpay):** Webhook-driven payment processing via `booking-service` enforcing signature validation and idempotency key checks.
2. **Notification Providers (SendGrid / Twilio):** Asynchronous email and SMS dispatch triggered by RabbitMQ events (`notification.user.queue`).
3. **Identity Providers (Google / GitHub OAuth2):** Federated single sign-on (SSO) integrated into `auth-service` returning standardized JWT tokens.

---

## 6. Related Documentation

- [Engineering Constitution](../engineering-kit/00-ENGINEERING-CONSTITUTION.md)
- [System Overview](./01-system-overview.md)
- [API Contracts](./06-api-contracts.md)
- [RabbitMQ Events](./09-rabbitmq-events.md)
