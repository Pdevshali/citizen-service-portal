# 🏛️ Citizen Services Portal — Microservice Architecture

A production-inspired **Spring Boot microservices** project that simulates a government citizen-services portal. It demonstrates real-world event-driven architecture using **Apache Kafka**, inter-service communication via **OpenFeign**, and persistent storage with **PostgreSQL**.

---

## 📐 Architecture Overview

```
                          ┌─────────────────────────────────────────────────┐
                          │              Citizen Services Portal             │
                          └─────────────────────────────────────────────────┘
                                               │
              ┌────────────────────────────────┼─────────────────────────────┐
              │                                │                             │
   ┌──────────▼──────────┐       ┌─────────────▼──────────┐     ┌───────────▼──────────┐
   │   citizen-service   │       │     ekyc-service        │     │   document-service   │
   │      :8081          │       │        :8082            │     │       :8083          │
   │                     │       │                         │     │                      │
   │  - Registration     │       │  - KYC Session Mgmt     │     │ - Document Storage   │
   │  - Profile Mgmt     │       │  - Aadhaar OTP via      │     │ - Fetch Requests     │
   │  - KYC Initiation   │       │    Mock UIDAI           │     │ - Document Records   │
   │  - Doc Fetch Request│       │  - Feign → citizen-svc  │     │                      │
   └──────────┬──────────┘       └─────────────┬──────────┘     └──────────────────────┘
              │                                │
              │          Apache Kafka          │
              │  ┌─────────────────────────┐   │
              └──►  kyc.initiation.requested├───┘
                 │  kyc.verification.completed│
                 │  document.fetch.requested  │
                 │  document.fetch.completed  │
                 └─────────────────────────┘
```

---

## 🧩 Microservices

### 1. `citizen-service` (Port `8081`)
The core service managing citizen registration, profiles, KYC workflow, and service requests.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/citizens/register` | Register a new citizen |
| `GET`  | `/api/citizens/{id}/profile` | Get citizen profile |
| `GET`  | `/api/citizens/{id}/validate` | Check if citizen exists |
| `POST` | `/api/citizens/initiate-kyc` | Trigger KYC verification |
| `POST` | `/api/citizens/fetch-document` | Request document fetch from UIDAI |
| `POST` | `/api/citizens/{id}/request-certificate` | Submit a certificate request |
| `GET`  | `/api/citizens/{id}/services` | List available government services |

**Kafka Events Published:**
- `kyc.initiation.requested` → consumed by `ekyc-service`
- `document.fetch.requested` → consumed by `document-service`

**Kafka Events Consumed:**
- `kyc.verification.completed` ← published by `ekyc-service`
- `document.fetch.completed` ← published by `document-service`

---

### 2. `ekyc-service` (Port `8082`)
Handles eKYC sessions, orchestrates Aadhaar-based OTP verification via the Mock UIDAI service, and reports results back.

**Key Features:**
- Manages `KycSession` lifecycle (`PENDING` → `IN_PROGRESS` → `VERIFIED` / `FAILED`)
- Calls a mock UIDAI service via `WebClient` for OTP-based Aadhaar verification
- Uses a **Feign client** to call back to `citizen-service` for citizen validation
- Consumes `kyc.initiation.requested` and publishes `kyc.verification.completed`

**Kafka Events Consumed:**
- `kyc.initiation.requested` ← published by `citizen-service`

**Kafka Events Published:**
- `kyc.verification.completed` → consumed by `citizen-service`

---

### 3. `document-service` (Port `8083`)
Manages citizen document records, handles async document fetch requests, and stores document metadata.

**Key Features:**
- Stores `DocumentRecord` entities with status tracking (`PENDING`, `FETCHED`, `FAILED`)
- Consumes `document.fetch.requested` events and simulates document retrieval
- Publishes `document.fetch.completed` events back to `citizen-service`

---

## ⚙️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.4.5 |
| **Language** | Java 21 |
| **Messaging** | Apache Kafka (Confluent Platform 7.4.0) |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Service Communication** | Spring Cloud OpenFeign |
| **Reactive HTTP** | Spring WebFlux (`WebClient`) |
| **Build Tool** | Maven |
| **Utilities** | Lombok, Jackson JSR310 |
| **Containerization** | Docker / Docker Compose |

---

## 🗃️ Data Models

### `Citizen` (citizen-service)
```
id              UUID (PK)
fullName        String
email           String (unique)
phone           String (unique)
dateOfBirth     LocalDate
aadhaarNumber   String (unique)
address         String
state           String
pincode         String (6)
kycStatus       Enum: PENDING | IN_PROGRESS | VERIFIED | FAILED
kycInitiatedAt  LocalDateTime
kycVerifiedAt   LocalDateTime
registeredAt    LocalDateTime
updatedAt       LocalDateTime
```

### `KycSession` (ekyc-service)
```
id            UUID (PK)
citizenId     String
aadhaarNumber String
kycStatus     Enum: PENDING | IN_PROGRESS | VERIFIED | FAILED
createdAt     LocalDateTime
updatedAt     LocalDateTime
```

### `DocumentRecord` (document-service)
```
id           UUID (PK)
citizenId    String
documentType Enum: AADHAAR | PAN | DRIVING_LICENSE | PASSPORT | VOTER_ID
fetchStatus  Enum: PENDING | FETCHED | FAILED
fetchedAt    LocalDateTime
```

---

## 📨 Kafka Topics & Event Flow

```
citizen-service  ──[kyc.initiation.requested]──►  ekyc-service
                 ◄─[kyc.verification.completed]──  ekyc-service

citizen-service  ──[document.fetch.requested]───►  document-service
                 ◄─[document.fetch.completed]────  document-service
```

| Topic | Producer | Consumer | Description |
|-------|----------|----------|-------------|
| `kyc.initiation.requested` | citizen-service | ekyc-service | Triggers a new KYC session |
| `kyc.verification.completed` | ekyc-service | citizen-service | Updates citizen KYC status |
| `document.fetch.requested` | citizen-service | document-service | Requests document retrieval |
| `document.fetch.completed` | document-service | citizen-service | Signals document fetch result |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

---

### 1. Start Infrastructure (Kafka + Zookeeper)

```bash
# From the project root
docker-compose up -d
```

This starts:
- **Zookeeper** on port `2181`
- **Kafka** on port `9092`

---

### 2. Set Up PostgreSQL Databases

Create the required databases in your PostgreSQL instance:

```sql
CREATE DATABASE "citizenDb";
CREATE DATABASE "kycSessionDb";
CREATE DATABASE "documentDb";
```

> ⚠️ Default credentials used in config: `username: postgres1`, `password: mypassword`.
> Update the `application.yml` / `application.yaml` files for each service if your credentials differ.

---

### 3. Run Each Microservice

Open separate terminals (or use your IDE) and run each service:

```bash
# Terminal 1 — Citizen Service
cd citizen-service
./mvnw spring-boot:run

# Terminal 2 — eKYC Service
cd ekyc-service
./mvnw spring-boot:run

# Terminal 3 — Document Service
cd document-service
./mvnw spring-boot:run
```

---

### 4. Verify Services are Running

| Service | Health Check URL |
|---------|-----------------|
| citizen-service | `http://localhost:8081/api/citizens` |
| ekyc-service | `http://localhost:8082` |
| document-service | `http://localhost:8083` |

---

## 🧪 Sample API Usage

### Register a Citizen
```bash
curl -X POST http://localhost:8081/api/citizens/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "phone": "9876543210",
    "dateOfBirth": "1995-06-15",
    "aadhaarNumber": "123456789012",
    "address": "123, MG Road",
    "state": "Maharashtra",
    "pincode": "411001"
  }'
```

### Initiate KYC
```bash
curl -X POST http://localhost:8081/api/citizens/initiate-kyc \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "<citizen-uuid>",
    "aadhaarNumber": "123456789012"
  }'
```

### Fetch a Document
```bash
curl -X POST http://localhost:8081/api/citizens/fetch-document \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "<citizen-uuid>",
    "documentType": "AADHAAR"
  }'
```

---

## 📁 Project Structure

```
citizen-services-portal-microservice/
│
├── citizen-service/                  # Core citizen management service
│   └── src/main/java/com/pdev/citizen_service/
│       ├── controller/               # REST endpoints
│       ├── service/                  # Business logic
│       ├── model/                    # JPA entities (Citizen, Document, etc.)
│       ├── dto/                      # Request/Response DTOs
│       ├── kafka/
│       │   ├── events/               # KycInitiationEvent, KycCompletedEvent, etc.
│       │   ├── producer/             # Kafka publishers
│       │   └── consumer/             # Kafka listeners
│       ├── repository/               # Spring Data JPA repositories
│       └── exception/                # Custom exception handling
│
├── ekyc-service/                     # eKYC verification service
│   └── src/main/java/com/pdev/ekyc_service/
│       ├── controller/
│       ├── service/
│       ├── model/                    # KycSession, KycStatus
│       ├── dto/
│       ├── kafka/
│       │   ├── events/               # KycInitiationEvent, KycCompletedEvent
│       │   ├── producer/
│       │   └── consumer/
│       ├── client/                   # CitizenServiceClient (Feign)
│       ├── config/
│       ├── util/
│       └── exception/
│
├── document-service/                 # Document management service
│   └── src/main/java/com/pdev/document_service/
│       ├── controller/
│       ├── service/
│       ├── model/                    # DocumentRecord, DocumentType, FetchStatus
│       ├── dto/
│       ├── kafka/
│       ├── client/
│       ├── config/
│       └── exception/
│
└── docker-compose.yml                # Kafka + Zookeeper infrastructure
```

---

## 🔧 Configuration Reference

### citizen-service (`application.yml`)
| Property | Value |
|----------|-------|
| Server Port | `8081` |
| Database | `citizenDb` (PostgreSQL @ `5433`) |
| Kafka Bootstrap | `localhost:9092` |
| Consumer Group | `citizen-service-group` |

### ekyc-service (`application.yaml`)
| Property | Value |
|----------|-------|
| Server Port | `8082` |
| Database | `kycSessionDb` (PostgreSQL @ `5432`) |
| Kafka Bootstrap | `localhost:9092` |
| Consumer Group | `ekyc-service-group` |
| Feign → citizen-service | `http://localhost:8081` |

---

## 🛣️ Roadmap

- [ ] Add an API Gateway (Spring Cloud Gateway)
- [ ] Integrate Keycloak for OAuth2 / JWT-based authentication
- [ ] Add a mock-uidai-service for complete Aadhaar OTP simulation
- [ ] Implement certificate-service for digital certificate generation
- [ ] Add distributed tracing (Zipkin / Micrometer)
- [ ] Add service discovery (Eureka)
- [ ] Containerize all microservices with Docker
- [ ] Write integration tests with Testcontainers

---

## 👤 Author

**Pdev** — Built as a learning project to demonstrate production-grade Spring Boot microservice patterns.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
