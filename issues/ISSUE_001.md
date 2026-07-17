# ISSUE-001 — OTP Verification Fails with Kafka TimeoutException

| Field         | Detail                                      |
|---------------|---------------------------------------------|
| **Serial No** | ISSUE-001                                   |
| **Date**      | 2026-07-15                                  |
| **Service**   | `ekyc-service` (port 8082)                  |
| **Severity**  | 🔴 Critical — Feature completely broken      |
| **Status**    | ✅ Resolved                                  |

---

## 📋 Description

When a user entered the OTP (`123456`) on the eKYC Verification page and clicked **"Verify OTP"**,
the frontend received an HTTP `500 Internal Server Error`. The backend `ekyc-service` crashed while
trying to publish a Kafka event after successful OTP verification.

---

## 🐛 Symptoms

### Frontend Console Error
```
POST http://localhost:8082/api/kyc/verify-otp  500 (Internal Server Error)
  at kyc.component.ts:80  →  onVerifyOtp()
```

### Backend Server Error (`ekyc-service` logs)
```
org.springframework.kafka.KafkaException: Send failed
  caused by: org.apache.kafka.common.errors.TimeoutException:
  Topic kyc.verification.completed not present in metadata after 60000 ms.

ERROR c.p.e.exception.GlobalExceptionHandler : Unexpected error occurred:
  Verification failed due to error
  java.lang.RuntimeException: Verification failed due to error
```

---

## 🔍 Root Cause Analysis

Two separate issues combined to produce this error:

---

### Bug #1 — Hardcoded Wrong Kafka Bootstrap Port

**File:** `ekyc-service/src/main/java/com/pdev/ekyc_service/config/KafkaConfig.java`

The `producerFactory` and `consumerFactory` beans had the Kafka broker address
**hardcoded to `localhost:9092`**, but the Docker Compose file exposes Kafka on **`localhost:39092`**.

```java
// ❌ BEFORE (wrong port — broker unreachable)
configProps.put("bootstrap.servers", "localhost:9092");

// ✅ AFTER (correct port matching docker-compose.yml)
configProps.put("bootstrap.servers", "localhost:39092");
```

Because the producer could not connect to the broker, it could never fetch topic
metadata — hence the 60-second `TimeoutException`.

**Docker Compose reference:**
```yaml
# docker-compose.yml
kafka:
  ports:
    - "39092:39092"   # ← host-accessible port is 39092, NOT 9092
  environment:
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:39092, ...
```

> **Note:** `application.yaml` already had the correct value
> `${KAFKA_BOOTSTRAP_SERVERS:localhost:39092}` but `KafkaConfig.java` bypassed it
> by hardcoding the port directly in Java — so the YAML config was never used.

---

### Bug #2 — Kafka Topics Were Never Created

**Container:** `kafka-init`

The `docker-compose.yml` includes a `kafka-init` service that creates the required
topics on first launch:

```yaml
kafka-init:
  command: |
    kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists
      --topic kyc.verification.completed ...
    kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists
      --topic kyc.initiation.event ...
```

However, the `kafka-init` container had already **exited** (it is a one-shot init
container) and the topics did **not persist** across a Docker restart/recreation.
When the service tried to publish to `kyc.verification.completed`, Kafka had no
record of it.

**Verified via:**
```bash
docker exec kafka kafka-topics --bootstrap-server kafka:29092 --list
# Result: Only __consumer_offsets was present — neither kyc topic existed.
```

---

## ✅ Fix Applied

### Fix 1 — Corrected Bootstrap Servers in `KafkaConfig.java`

Both `producerFactory` and `consumerFactory` beans updated:

```java
// producerFactory
configProps.put("bootstrap.servers", "localhost:39092");  // was: 9092

// consumerFactory
configProps.put("bootstrap.servers", "localhost:39092");  // was: 9092
```

### Fix 2 — Manually Created Missing Kafka Topics

Topics created directly inside the running Kafka container:

```bash
docker exec kafka kafka-topics \
  --bootstrap-server kafka:29092 \
  --create --if-not-exists \
  --topic kyc.verification.completed \
  --partitions 1 --replication-factor 1

docker exec kafka kafka-topics \
  --bootstrap-server kafka:29092 \
  --create --if-not-exists \
  --topic kyc.initiation.event \
  --partitions 1 --replication-factor 1
```

### Fix 3 — Added Auto Topic Creation via `KafkaAdmin` (Prevention)

Added `KafkaAdmin` + `NewTopic` Spring beans to `KafkaConfig.java` so topics are
**automatically created at service startup** if they don't exist — preventing this
issue from recurring after a fresh Docker container restart:

```java
private static final String BOOTSTRAP_SERVERS = "localhost:39092";

@Bean
public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    return new KafkaAdmin(configs);
}

@Bean
public NewTopic kycVerificationCompletedTopic() {
    return TopicBuilder.name("kyc.verification.completed")
            .partitions(1).replicas(1).build();
}

@Bean
public NewTopic kycInitiationEventTopic() {
    return TopicBuilder.name("kyc.initiation.event")
            .partitions(1).replicas(1).build();
}
```

---

## 📁 Files Changed

| File                                                       | Change                                                           |
|------------------------------------------------------------|------------------------------------------------------------------|
| `ekyc-service/.../config/KafkaConfig.java`                 | Fixed bootstrap port `9092` → `39092`; added `KafkaAdmin` + `NewTopic` beans |

---

## 🔁 How to Reproduce (Before Fix)

1. Start Docker Compose services (Postgres, Kafka, Zookeeper)
2. Start `ekyc-service` Spring Boot app
3. Login as a citizen → navigate to **KYC** page
4. Click **"Initiate KYC"** — OTP is sent
5. Enter OTP `123456` → click **"Verify OTP"**
6. ❌ Frontend shows: *"An unexpected error occurred"*
7. Backend logs show `KafkaException: Send failed` with `TimeoutException`

---

## ✔️ Verification (After Fix)

1. Kafka topics confirmed present via:
   ```bash
   docker exec kafka kafka-topics --bootstrap-server kafka:29092 --describe \
     --topic kyc.verification.completed
   # Topic: kyc.verification.completed  PartitionCount: 1  ReplicationFactor: 1
   ```
2. `ekyc-service` restarted with fixed `KafkaConfig.java`
3. OTP verification flow completes successfully — `KycStatus.VERIFIED` returned to frontend

---

## 💡 Lessons Learned

| # | Lesson |
|---|--------|
| 1 | Never hardcode infrastructure ports in Spring `@Configuration` — use `application.yaml` with `${ENV_VAR:default}` and honour it in Java config. |
| 2 | Always add `KafkaAdmin` + `NewTopic` `@Bean`s for auto topic creation — don't rely solely on one-shot Docker init containers. |
| 3 | One-shot `kafka-init` containers are fragile on re-runs; Spring's `KafkaAdmin` is a more reliable and portable alternative. |
