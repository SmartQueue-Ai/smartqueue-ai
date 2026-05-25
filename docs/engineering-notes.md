# SmartQueue AI — Engineering Notes

This document tracks:

- engineering learnings
- debugging notes
- architecture decisions
- infrastructure observations
- distributed systems insights
- DevOps learnings
- collaboration observations

---

# Day-0 Notes

## Repository & Workflow Initialization

Completed:

- GitHub organization setup
- repository initialization
- branch strategy planning
- collaborative engineering workflow setup
- initial monorepo structure setup

---

# Branching Strategy

Workflow:

```text
feature/* → develop → main
```

## Reasoning

This workflow simulates real engineering teams.

Benefits:

- protects stable code
- enables safe collaboration
- prevents unstable direct pushes
- improves review culture
- supports scalable development workflow

---

# Initial Monorepo Architecture

Current structure:

```text
frontend/
backend/
ai-services/
infrastructure/
docs/
scripts/
```

Backend services:

```text
auth-service/
booking-service/
inventory-service/
realtime-gateway/
```

## Why Monorepo?

The monorepo architecture improves:

- shared visibility
- dependency management
- infrastructure consistency
- DevOps standardization
- onboarding simplicity

This structure also supports future microservice scaling.

---

# Key Engineering Principles

## 1. Infrastructure First

The project prioritizes:

- architecture quality
- scalability
- maintainability
- observability
- distributed systems understanding

before feature implementation.

Reason:

Poor foundations create long-term technical debt.

---

## 2. Shared Ownership

Both contributors should:

- understand all systems
- contribute equally
- review each other’s work
- participate in architecture decisions
- avoid isolated ownership

The goal is collaborative engineering growth.

---

## 3. Production Thinking

Every implementation should answer:

- Why does this exist?
- What scalability issue does it solve?
- How would this behave under high traffic?
- How would debugging work in production?
- How would this scale horizontally?

---

# Planned Infrastructure Stack

## Backend

- Spring Boot microservices
- Node.js realtime gateway

---

## Databases

- PostgreSQL
- Redis
- MongoDB (future)

---

## Messaging

- RabbitMQ

---

## AI Infrastructure

- FastAPI
- Gemini API
- LangChain
- ChromaDB

---

## DevOps

- Docker
- Docker Compose
- Kubernetes
- GitHub Actions

---

# Upcoming Infrastructure Tasks

Next planned engineering tasks:

- Docker setup
- Docker Compose setup
- PostgreSQL integration
- Redis integration
- RabbitMQ integration
- Spring Boot service bootstrap
- service networking
- health endpoints
- container debugging
- environment variable management

---

# Distributed Systems Learning Focus

Current concepts being studied:

- distributed locking
- queue systems
- asynchronous messaging
- service isolation
- horizontal scalability
- realtime synchronization
- concurrency handling

---

# Redis Learning Goals

Redis will later power:

- distributed locking
- temporary seat locking
- queue management
- rate limiting
- caching
- session management

Important future concept:

```text
SETNX
```

for distributed locks.

---

# RabbitMQ Learning Goals

RabbitMQ will later handle:

- booking events
- retry workflows
- notification pipelines
- analytics events

Purpose:

Enable asynchronous microservice communication.

---

# Docker Learning Goals

Docker is being introduced early to ensure:

- reproducible environments
- consistent deployments
- isolated services
- production-style local infrastructure

Key concepts to understand:

- containers
- images
- networks
- volumes
- service discovery

---

# Common Git Commands

## Create Feature Branch

```bash
git checkout develop
git pull
git checkout -b feature/<feature-name>
```

---

## Push Feature Branch

```bash
git push origin feature/<feature-name>
```

---

## Sync With Develop

```bash
git checkout develop
git pull
```

---

# Debugging Notes

## Common Git Issue

If push fails:

```bash
git pull origin develop
```

then retry push.

---

## Common Docker Issue

Inside containers:

```text
localhost != host machine
```

Services communicate using container names.

Example:

```text
postgres
redis
rabbitmq
```

NOT:

```text
localhost
```

---

# Engineering Philosophy Reminder

Prioritize:

- engineering quality
- scalability understanding
- backend depth
- architecture quality
- production thinking

NOT:

- flashy UI
- shallow implementation
- tutorial-copy development

---

# Long-Term Goal

Build a flagship engineering project demonstrating:

- distributed systems understanding
- backend engineering depth
- scalable architecture
- DevOps maturity
- realtime infrastructure
- AI integration
- production-grade thinking

---

# Current Status

Day-0:

- organization setup completed
- repository setup completed
- branch workflow established
- monorepo setup in progress
- infrastructure bootstrap beginning