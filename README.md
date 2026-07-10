# Citizen Services Portal - Microservice Architecture

A Spring Boot microservices project that simulates a citizen-services portal. The system uses event-driven communication with Apache Kafka, service discovery with Eureka, PostgreSQL persistence, and an Angular frontend for citizen-facing workflows.

## Current Architecture

```text
Angular frontend (:4200)
        |
        v
citizen-service (:8081) <---- REST/Feign ----> ekyc-service (:8082)
        |                                           |
        |                                           +-- Mock UIDAI endpoints
        |
        +-- Kafka: kyc.initiation.requested ------> ekyc-service
        +-- Kafka: document.fetch.requested ------> document-service (:8083)
        +-- Kafka: certificate.issuance.requested -> certificate-service (:8084)

ekyc-service -------- Kafka: kyc.verification.completed ----+
document-service ---- Kafka: document.fetch.completed ------+--> citizen-service
certificate-service - Kafka: certificate.issued ------------+
                                                            |
                                                            +--> notification-service (:8087)

eureka-server (:8761) provides service discovery for backend services.
Kafka runs on localhost:9092 through the root docker-compose.yml.
```

## Modules

| Module | Port | Purpose |
| --- | ---: | --- |
| `eureka-server` | `8761` | Spring Cloud Netflix Eureka registry |
| `citizen-service` | `8081` | Citizen registration, profile, KYC initiation, document requests, certificate requests, available services |
| `ekyc-service` | `8082` | OTP-based KYC flow, mock UIDAI integration, KYC status |
| `document-service` | `8083` | Async document fetch handling and basic document list endpoint |
| `certificate-service` | `8084` | Async certificate issuance and certificate lookup endpoints |
| `notification-service` | `8087` | Consumes domain events and records/sends notifications through configured channels |
| `frontend` | `4200` | Angular citizen portal UI |

## Tech Stack

| Area | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.4.5 |
| Spring Cloud | Spring Cloud 2024.0.1, Eureka, OpenFeign, LoadBalancer |
| Messaging | Apache Kafka, Confluent Platform 7.4.0 |
| Database | PostgreSQL |
| Persistence | Spring Data JPA / Hibernate, Flyway in `citizen-service` |
| Frontend | Angular 19.2, TypeScript 5.7 |
| Build | Maven wrappers per backend service, Angular CLI |
| Utilities | Lombok, Jackson JSR310, Spring Mail |
| Infrastructure | Docker Compose for Kafka and Zookeeper |

## REST API Summary

### citizen-service

Base URL: `http://localhost:8081/api/citizens`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/register` | Register a citizen |
| `GET` | `/{id}/validate` | Check whether a citizen exists |
| `GET` | `/{id}/profile` | Get citizen profile |
| `POST` | `/initiate-kyc` | Start KYC through Kafka |
| `POST` | `/fetch-document` | Request document fetch through Kafka |
| `POST` | `/{id}/request-certificate` | Request certificate issuance through Kafka |
| `GET` | `/{id}/services` | List available government services |

### ekyc-service

Base URL: `http://localhost:8082`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/kyc/generate-otp` | Generate OTP for Aadhaar KYC |
| `POST` | `/api/kyc/verify-otp` | Verify OTP and complete KYC |
| `GET` | `/api/kyc/{citizenId}/status` | Get current KYC status |
| `POST` | `/mock-uidai/otp/generate` | Mock UIDAI OTP generation |
| `POST` | `/mock-uidai/otp/verify` | Mock UIDAI OTP verification; test OTP is `123456` |
| `GET` | `/mock-uidai/health` | Mock UIDAI health/status |
| `DELETE` | `/mock-uidai/reset` | Clear mock UIDAI sessions |

### document-service

Base URL: `http://localhost:8083/api/documents`

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/{citizenId}/list` | List documents for a citizen |

### certificate-service

Base URL: `http://localhost:8084/api/certificates`

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/{id}` | Get certificate details and status |
| `GET` | `/citizen/{citizenId}` | List certificates for a citizen |

## Kafka Topics

| Topic | Producer | Consumer(s) | Purpose |
| --- | --- | --- | --- |
| `kyc.initiation.requested` | `citizen-service` | `ekyc-service` | Starts KYC processing |
| `kyc.verification.completed` | `ekyc-service` | `citizen-service`, `notification-service` | Reports KYC result |
| `document.fetch.requested` | `citizen-service` | `document-service` | Requests document retrieval |
| `document.fetch.completed` | `document-service` | `citizen-service`, `notification-service` | Reports document availability |
| `certificate.issuance.requested` | `citizen-service` | `certificate-service` | Requests certificate generation |
| `certificate.issued` | `certificate-service` | `citizen-service`, `notification-service` | Reports issued certificate |
| `grievance.sla.breached` | Future grievance flow | `notification-service` | Notification hook already present in consumer |

## Databases

The services currently use local PostgreSQL connections with the username `postgres1` and password `mypassword`.

| Service | Database URL |
| --- | --- |
| `citizen-service` | `jdbc:postgresql://localhost:5433/citizenDb` |
| `ekyc-service` | `jdbc:postgresql://localhost:5432/kycSessionDb` |
| `document-service` | `jdbc:postgresql://localhost:5434/documentdb` |
| `certificate-service` | `jdbc:postgresql://localhost:5435/certificateDb` |
| `notification-service` | `jdbc:postgresql://localhost:5432/notificationDb` |

Create the databases that match your local PostgreSQL setup, or update each service's `application.yml` / `application.yaml`.

```sql
CREATE DATABASE "citizenDb";
CREATE DATABASE "kycSessionDb";
CREATE DATABASE "documentdb";
CREATE DATABASE "certificateDb";
CREATE DATABASE "notificationDb";
```

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js and npm for the Angular frontend
- Docker and Docker Compose
- PostgreSQL

### 1. Start Kafka and Zookeeper

From the project root:

```bash
docker-compose up -d
```

This starts:

- Zookeeper on `localhost:2181`
- Kafka on `localhost:9092`

### 2. Start Eureka

```bash
cd eureka-server
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

Eureka dashboard: `http://localhost:8761`

### 3. Start Backend Services

Run each service in its own terminal after Kafka, PostgreSQL, and Eureka are available:

```bash
cd citizen-service
./mvnw spring-boot:run

cd ekyc-service
./mvnw spring-boot:run

cd document-service
./mvnw spring-boot:run

cd certificate-service
./mvnw spring-boot:run

cd notification-service
./mvnw spring-boot:run
```

On Windows PowerShell, use `.\mvnw.cmd spring-boot:run`.

### 4. Start the Angular Frontend

```bash
cd frontend
npm install
npm start
```

Frontend URL: `http://localhost:4200`

Frontend routes currently include:

- `/`
- `/register`
- `/profile/:id`
- `/kyc`
- `/documents`
- `/certificates`
- `/services/:id`

## Sample API Calls

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

### Generate and Verify KYC OTP

```bash
curl -X POST http://localhost:8082/api/kyc/generate-otp \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "<citizen-uuid>",
    "aadhaarNumber": "123456789012"
  }'
```

```bash
curl -X POST http://localhost:8082/api/kyc/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "<citizen-uuid>",
    "txnId": "<txn-id-from-generate-otp>",
    "otp": "123456"
  }'
```

### Request a Document

```bash
curl -X POST http://localhost:8081/api/citizens/fetch-document \
  -H "Content-Type: application/json" \
  -d '{
    "citizenId": "<citizen-uuid>",
    "documentType": "AADHAAR"
  }'
```

### Request a Certificate

```bash
curl -X POST http://localhost:8081/api/citizens/<citizen-uuid>/request-certificate \
  -H "Content-Type: application/json" \
  -d '{
    "certificateType": "INCOME_CERTIFICATE"
  }'
```

### Check Issued Certificates

```bash
curl http://localhost:8084/api/certificates/citizen/<citizen-uuid>
```

## Project Structure

```text
citizen-services-portal-microservice/
|-- citizen-service/        # Core citizen workflow service
|-- ekyc-service/           # KYC and mock UIDAI service
|-- document-service/       # Document fetch service
|-- certificate-service/    # Certificate issuance service
|-- notification-service/   # Notification event consumer/service
|-- eureka-server/          # Service discovery server
|-- frontend/               # Angular 19 citizen portal
|-- docker-compose.yml      # Kafka and Zookeeper infrastructure
|-- DATABASE_CONSTRAINT_FIX_DOCUMENTATION.txt
`-- deep-dive-projects.html
```

## Notes

- The root `docker-compose.yml` only starts Kafka and Zookeeper. Databases are expected to be provided separately unless you use the service-specific compose files.
- All backend services register with Eureka at `http://localhost:8761/eureka/`.
- Kafka topic auto-creation is enabled in the root compose file.
- `citizen-service` uses Flyway with `ddl-auto: validate`; make sure migrations and database schema are aligned before startup.
- `notification-service` supports mail configuration through `MAIL_*` environment variables and defaults to a local SMTP host on port `1025`.
