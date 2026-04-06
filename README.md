# 🛡️ SmartSure - Insurance Management System

![Project Status](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)
![Tech Stack](https://img.shields.io/badge/Stack-Microservices-blue?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20Spring%20Security-red?style=for-the-badge)

**SmartSure** is a state-of-the-art, microservices-based insurance management platform. It provides a seamless experience for customers to browse policies, purchase insurance, and file claims, while giving administrators powerful tools for reviewing applications and generating financial reports.

---

## 🏗️ System Architecture

SmartSure is built on a highly decoupled architecture using **Spring Boot 3.5.x** and **Spring Cloud**. All services are containerized and communicate through a secure, orchestrated network.

### 🧩 Microservices Overview
- **🛡️ Auth Service (8002)**: Handles user registration, JWT-based authentication, and OTP-verified registration.
- **💼 Policy Service (8004)**: Manages the policy catalog (Health, Vehicle, Life), policy purchasing, and user-to-policy mapping.
- **📄 Claims Service (8003)**: Manages the lifecycle of insurance claims, including document uploads and status tracking.
- **👔 Admin Service (8001)**: Orchestrates administrative reviews, policy CRUD, and cross-service reporting.
- **💳 Payment Service (8085)**: Integrated with **Razorpay** for secure premium payments and order tracking.
- **🔔 Notification Service (8006)**: An asynchronous service that handles email notifications (Policy Purchase, Cancellation, OTPs) using **RabbitMQ** and **SMTP**.

### ⚙️ Infrastructure & Cloud
- **🚦 API Gateway (8888)**: The centralized entry point with `JwtAuthenticationFilter` and route validation.
- **📡 Eureka Server (8761)**: Service discovery registry for dynamic networking.
- **📁 Config Server (9999)**: Centralized configuration management using the Native profile (linked to a Git repository).
- **📝 RabbitMQ (5672)**: Messaging broker for asynchronous notifications and inter-service events.

---

## 🛠️ Technology Stack

### **Frontend**
- **Core**: React 19 (TypeScript), Vite
- **Styling**: TailwindCSS 4 (Premium Modern UI)
- **State Management**: Redux Toolkit & RTK Query
- **Animations**: Framer Motion
- **Performance**: Optimized with **Route-based Lazy Loading** for fast initial paints.

### **Backend**
- **Framework**: Spring Boot 3.5.x
- **Microservices**: Spring Cloud (Gateway, Eureka, Config, Feign)
- **Security**: JWT (jjwt 0.12.6), Spring Security, BCrypt
- **Persistence**: PostgreSQL 16 (Main Data Store), Redis (Caching)
- **Communication**: OpenFeign (Synchronous), RabbitMQ (Asynchronous)
- **Monitoring**: Actuator, Prometheus, Micrometer

### **Observability & DevOps**
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Zipkin (Distributed tracing via MySQL storage)
- **Metrics**: Prometheus & Grafana (Real-time JVM and request tracking)
- **Containerization**: Docker & Docker Compose

---

## ✨ Key Features & Optimizations

- **⚡ High Performance**: Implemented **Code Splitting** in the frontend, ensuring only necessary code is loaded per route.
- **✉️ Async Communications**: Policy confirmations and OTP emails are processed via background RabbitMQ tasks, ensuring the main application remains responsive.
- **🔒 Multi-Layer Security**: 
  - **Gateway Layer**: JWT validation and header injection.
  - **Network Layer**: `X-Gateway-Secret` validation to block direct access to internal microservices.
  - **Method Layer**: Fine-grained `@PreAuthorize` controls.
- **💰 Razorpay Integration**: Fully functional payment gateway for policy purchases.
- **🛡️ Resilience**: Implemented **Spring Retry** in the Admin orchestrator for fault-tolerant cross-service calls.

---

## 🚀 Getting Started

### 📋 Prerequisites
- **Java 17+**
- **Node.js 20+**
- **Docker & Docker Compose**
- **Maven 3.9+**

### 🐳 Running with Docker (Recommended)
The entire ecosystem (DBs, RabbitMQ, Redis, 8 Microservices, and Frontend) can be started with a single command:

```bash
docker-compose up -d --build
```

### 💻 Local Development Setup
1. **Start Infrastructure**: Ensure Postgres, RabbitMQ, and Redis are running.
2. **Start Discovery & Config**: Launch `eureka-server` and `config-server` first.
3. **Start Authentication**: Launch `auth-service`.
4. **Launch Business Services**: `policy`, `claims`, `admin`, `payment`, `notification`.
5. **Start Gateway**: Launch `api-gateway` (8888).
6. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 📂 Project Structure
```text
├── .agents/              # AI configuration and skills
├── backend/
│   ├── admin-service/    # Admin orchestrator
│   ├── api-gateway/      # Routing & Security
│   ├── auth-service/     # Identity & Access
│   ├── claims-service/   # Claims Lifecycle
│   ├── config-server/    # Service Configs
│   ├── eureka-server/    # Service Registry
│   ├── notification/     # SMTP & Messaging
│   ├── payment-service/  # Razorpay integration
│   ├── policy-service/   # Policy Catalog
│   └── docs/             # Architecture (HLD/LLD) & API Specs
├── docker/               # Initialization scripts & Logstash config
├── frontend/             # React + Vite application
└── docker-compose.yml    # Full stack orchestration
```

---
© 2026 SmartSure Insurance Management System.
