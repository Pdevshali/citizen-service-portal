# Citizen Services Portal — Microservice Architecture

A Spring Boot microservices project simulating a government citizen-services portal. Citizens register, complete Aadhaar-based KYC, fetch documents, request certificates, and file grievances — all through an Angular frontend backed by event-driven microservices communicating via Apache Kafka.

---

## Architecture

```
Angular Frontend (:4200)
        │  (JWT Bearer token from Keycloak)
        ▼
  API Gateway (:8080)  ←── Keycloak JWT validation, CORS, load-balancing
        │
        ├──► citizen-service (:8081)   ← core: registration, KYC, docs, certs, services
        ├──► ekyc-service    (:8082)   ← OTP-based KYC, mock UIDAI
        ├──► document-service(:8083)   ← async document fetch
        ├──► certificate-service(:8084)← async certificate issuance
        ├──► grievance-service(:8085)  ← grievance filing and tracking
        └──► notification-service(:8087)← event-driven notifications

All services register with → eureka-server (:8761)

Kafka event flow:
  citizen-service  ──kyc.initiation.event──────────► ekyc-service
  ekyc-service     ──kyc.verification.completed──────► citizen-service, notification-service
  citizen-service  ──document.fetch.requested─────────► document-service
  citizen-service  ──certificate.issuance.requested──► certificate-service
  citizen-service  ──grievance.submitted───────────────► grievance-service, notification-service
```

---

## Services & Ports

| Service | Port | DB Port | Purpose |
|---|---:|---:|---|
| `eureka-server` | `8761` | — | Spring Cloud Eureka service registry |
| `api-gateway` | `8080` | — | JWT validation, routing, CORS |
| `citizen-service` | `8081` | `5433` | Registration, KYC initiation, documents, certificates |
| `ekyc-service` | `8082` | `5437` | OTP-based Aadhaar KYC, mock UIDAI integration |
| `document-service` | `8083` | `5434` | Async document fetch & storage |
| `certificate-service` | `8084` | `5435` | Async certificate issuance |
| `grievance-service` | `8085` | `5438` | Grievance submission & tracking |
| `notification-service` | `8087` | `5436` | Consumes events, sends email notifications |
| `frontend` | `4200` | — | Angular 19 citizen portal UI |
| **Keycloak** | `8180` | internal | Identity & access management |

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.4.5 |
| Spring Cloud | Spring Cloud 2024.0.1, Eureka, Gateway, OpenFeign, LoadBalancer |
| Security | Keycloak 24.0.3, OAuth2 / JWT (Resource Server) |
| Messaging | Apache Kafka (Confluent 7.4.0), Zookeeper |
| Database | PostgreSQL 16 (one isolated DB per service) |
| Persistence | Spring Data JPA / Hibernate, Flyway (citizen-service) |
| Frontend | Angular 19.2, TypeScript 5.7, Keycloak-Angular |
| Build | Maven wrappers per backend service, Angular CLI 19 |
| Infrastructure | Docker Compose (all infra in root `docker-compose.yml`) |
| Utilities | Lombok, Jackson JSR310, Spring Mail |

---

## Getting Started (Local Development)

### Prerequisites

- Java 21+
- Maven (via `mvnw` wrapper — no install needed)
- Node.js 20+ & npm
- Docker Desktop

### Step 1 — Start All Infrastructure

From the project root (starts Kafka, Zookeeper, Keycloak, and **all 6 PostgreSQL databases**):

```cmd
docker compose up -d
```

Wait ~60 seconds for Keycloak to initialize. Verify:
```cmd
curl http://localhost:8180/health/ready
```

> **First-time only:** Set up Keycloak — see [Keycloak Setup](#keycloak-setup) below.

### Step 2 — Start Eureka Server

Open a new CMD window:

```cmd
cd eureka-server
mvnw spring-boot:run
```

Verify at: `http://localhost:8761`

### Step 3 — Start Backend Services

Open a **separate CMD window** for each service. Set env vars before running:

**citizen-service:**
```cmd
cd citizen-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/citizenDb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/citizen-portal
mvnw spring-boot:run
```

**ekyc-service:**
```cmd
cd ekyc-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5437/kycSessionDb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/citizen-portal
mvnw spring-boot:run
```

**document-service:**
```cmd
cd document-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/documentdb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
mvnw spring-boot:run
```

**certificate-service:**
```cmd
cd certificate-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/certificateDb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
mvnw spring-boot:run
```

**grievance-service:**
```cmd
cd grievance-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5438/grievanceDb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/citizen-portal
mvnw spring-boot:run
```

**notification-service:**
```cmd
cd notification-service
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5436/notificationDb
set SPRING_DATASOURCE_USERNAME=postgres1
set SPRING_DATASOURCE_PASSWORD=mypassword
set KAFKA_BOOTSTRAP_SERVERS=localhost:39092
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=your@gmail.com
set MAIL_PASSWORD=your_gmail_app_password
mvnw spring-boot:run
```

**api-gateway (start LAST):**
```cmd
cd api-gatway
set KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/citizen-portal
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set ALLOWED_ORIGINS=http://localhost:4200
mvnw spring-boot:run
```

### Step 4 — Start Angular Frontend

```cmd
cd frontend
npm install
npm start
```

Frontend: `http://localhost:4200`

---

## Keycloak Setup

> One-time setup required when running locally for the first time.

1. Open `http://localhost:8180` → login with `admin` / `admin_local_dev` (from `.env`)
2. **Create Realm**: name = `citizen-portal`
3. **Create Client**: Client ID = `citizen-portal-client`, type = OpenID Connect
   - Standard flow ON, Direct access grants ON
   - Valid redirect URIs: `http://localhost:4200/*`
   - Web origins: `http://localhost:4200`
4. **Create Realm Roles**: `CITIZEN`, `OFFICER`, `ADMIN`
5. **Create a test user** → set credentials → assign `CITIZEN` role

---

## Databases

Each service has its own isolated PostgreSQL container started by `docker compose up -d`.
No manual database creation is needed.

| Service | Database | Host Port |
|---|---|---:|
| `citizen-service` | `citizenDb` | `5433` |
| `ekyc-service` | `kycSessionDb` | `5437` |
| `document-service` | `documentdb` | `5434` |
| `certificate-service` | `certificateDb` | `5435` |
| `notification-service` | `notificationDb` | `5436` |
| `grievance-service` | `grievanceDb` | `5438` |
| `keycloak` | `keycloak` (internal) | internal |

> Port `5432` is intentionally left free for any local PostgreSQL installation.

**Flyway** (citizen-service only): Migrations run automatically on startup.
Migration files: `citizen-service/src/main/resources/db/migration/`

---

## Kafka Topics

| Topic | Producer | Consumer(s) |
|---|---|---|
| `kyc.initiation.event` | `citizen-service` | `ekyc-service` |
| `kyc.verification.completed` | `ekyc-service` | `citizen-service`, `notification-service` |
| `document.fetch.requested` | `citizen-service` | `document-service` |
| `certificate.issuance.requested` | `citizen-service` | `certificate-service` |
| `grievance.submitted` | `grievance-service` | `notification-service` |
| `notification.send` | multiple | `notification-service` |

All topics are auto-created by `kafka-init` container on `docker compose up`.

---

## REST API Summary

All requests go through the **API Gateway** at `http://localhost:8080` with a Keycloak Bearer token:

```
Authorization: Bearer <token>
```

### citizen-service — `/api/citizens`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/me/onboarding` | Onboard a new citizen (links Keycloak account) |
| `GET` | `/me` | Get current authenticated citizen profile |
| `POST` | `/initiate-kyc` | Initiate Aadhaar KYC (publishes Kafka event) |
| `POST` | `/fetch-document` | Request document fetch (publishes Kafka event) |
| `POST` | `/{id}/request-certificate` | Request certificate issuance |
| `GET` | `/{id}/services` | List available government services |

### ekyc-service — `/api/kyc`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/generate-otp` | Generate OTP for Aadhaar KYC |
| `POST` | `/verify-otp` | Verify OTP and complete KYC (test OTP: `123456`) |
| `GET` | `/{citizenId}/status` | Get KYC status for a citizen |
| `POST` | `/mock-uidai/otp/generate` | Mock UIDAI OTP generation endpoint |
| `POST` | `/mock-uidai/otp/verify` | Mock UIDAI OTP verify endpoint |
| `DELETE` | `/mock-uidai/reset` | Clear mock UIDAI sessions |

### document-service — `/api/documents`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/{citizenId}/list` | List fetched documents for a citizen |

### certificate-service — `/api/certificates`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/{id}` | Get certificate by ID |
| `GET` | `/citizen/{citizenId}` | List all certificates for a citizen |

### grievance-service — `/api/grievances`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Submit a new grievance |
| `GET` | `/citizen/{citizenId}` | List grievances for a citizen |
| `GET` | `/{id}` | Get grievance by ID |

---

## Environment Variables

All secrets are injected via environment variables — nothing is hardcoded in source or Docker images.

### Shared (all services)

| Variable | Local Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:39092` | Kafka broker URL |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://localhost:8761/eureka/` | Eureka URL |
| `SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL` | `PLAINTEXT` | `SASL_SSL` for Confluent Cloud |
| `SPRING_KAFKA_PROPERTIES_SASL_MECHANISM` | `PLAIN` | Confluent Cloud SASL mechanism |
| `SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG` | _(empty)_ | Confluent Cloud JAAS config |

### Per Service

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for the service's PostgreSQL DB |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URI |
| `ALLOWED_ORIGINS` | (api-gateway only) Allowed CORS origin |
| `MAIL_HOST/PORT/USERNAME/PASSWORD` | (notification-service only) SMTP config |

---

## Project Structure

```
citizen-services-portal-microservice/
├── api-gatway/                  # Spring Cloud Gateway (JWT + routing + CORS)
├── citizen-service/             # Core citizen workflows + Flyway migrations
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V0__Base_schema.sql
│           ├── V1__Fix_documents_constraint.sql
│           └── V2__add_keycloak_user_id.sql
├── ekyc-service/                # OTP-based KYC + mock UIDAI
├── document-service/            # Async document fetch
├── certificate-service/         # Async certificate issuance
├── grievance-service/           # Grievance filing & tracking
├── notification-service/        # Event-driven email notifications
├── eureka-server/               # Service discovery
├── frontend/                    # Angular 19 citizen portal
├── docker-compose.yml           # ALL infrastructure (Kafka, Keycloak, 6 × PostgreSQL)
├── .env                         # Local secrets (gitignored)
├── .env.example                 # Safe template to copy from
└── DEPLOYMENT_PLAN.md           # Render deployment guide
```

---

## Useful Commands

```cmd
# Check all running infra containers
docker ps

# See Kafka topics
docker exec -it kafka kafka-topics --bootstrap-server localhost:39092 --list

# Reset a citizen's KYC (if stuck in INITIATED)
docker exec -it postgres-citizen psql -U postgres1 -d citizenDb -c ^
  "UPDATE citizens SET kyc_status='PENDING', kyc_initiated_at=NULL WHERE id='<citizen-id>';"

# Stop all infrastructure
docker compose down

# Stop + wipe all database volumes (fresh start)
docker compose down -v
```

---

## Notes

- `set` commands in CMD are session-scoped — env vars reset when you close the window.
- KYC auto-retries after 10 minutes if stuck in `INITIATED` (handles ekyc-service downtime).
- `citizen-service` uses `ddl-auto: validate` + Flyway — schema is migration-managed.
- All other services use `ddl-auto: update` — Hibernate manages schema automatically.
- The API Gateway validates JWT tokens against Keycloak's JWKS before forwarding requests.
- `notification-service` uses Gmail SMTP — create an App Password in your Google account.
