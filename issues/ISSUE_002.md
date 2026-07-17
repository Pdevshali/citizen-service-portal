# ISSUE-002 — Profile in Navbar Shows Wrong Citizen (Stale citizenId)

| Field         | Detail                                              |
|---------------|-----------------------------------------------------|
| **Serial No** | ISSUE-002                                           |
| **Date**      | 2026-07-15                                          |
| **Service**   | `frontend` — Angular (port 4200)                    |
| **Severity**  | 🟠 High — Incorrect data shown to logged-in user    |
| **Status**    | ✅ Resolved                                          |

---

## 📋 Description

The **Home page** showed the correct currently-logged-in citizen profile (e.g. `test46`,
Citizen ID `37f359f5-...`), but clicking **Profile** in the navbar navigated to a
**completely different citizen's profile** (e.g. `test41`, Citizen ID `62b16bad-...`).

---

## 🐛 Symptoms

| Location | Citizen shown |
|----------|---------------|
| Home page "View My Profile" button | `test46` — `37f359f5-fb11-4806-91e3-8a86f6a05586` ✅ |
| Navbar → Profile link | `test41` — `62b16bad-acf1-40b9-9bcd-baca6e2c0340` ❌ |

The URL visible in the browser status bar:
```
localhost:4200/profile/62b16bad-acf1-40b9-9bcd-baca6e2c0340
```
This was a **different citizen's ID** from the one shown on the home page.

---

## 🔍 Root Cause Analysis

### The Angular Header Lifecycle Problem

`HeaderComponent` is a **persistent component** — it is created once when the app
loads and is **never destroyed** during client-side navigation. This means `ngOnInit()`
runs **only one time** at app start.

The original code read `citizenId` from `localStorage` inside `ngOnInit()`:

```typescript
// header.component.ts — BEFORE (broken)
ngOnInit() {
  this.citizenId = localStorage.getItem('citizenId') || '';  // read only once!
}
```

When a **new user registers**, `RegisterComponent` writes the new ID to `localStorage`:
```typescript
// register.component.ts — BEFORE (broken)
localStorage.setItem('citizenId', res.data.id);   // updates localStorage...
localStorage.setItem('citizenName', res.data.fullName);
// ...but HeaderComponent's citizenId property is NEVER updated!
```

**Result:** `localStorage` now contains `test46`'s ID (`37f359f5-...`), but the
already-alive `HeaderComponent` still holds `test41`'s ID (`62b16bad-...`) in its
`citizenId` property from when it first initialized.

The **Home page** worked correctly because `HomeComponent` is recreated on every
navigation, so its `ngOnInit()` always reads the fresh `localStorage` value.

### Why the IDs Differed

`62b16bad-...` (`test41`) was a **previously registered user** from an earlier session.
When `test46` registered in the same browser tab, the header was never told about the
change — it kept the old ID.

---

## ✅ Fix Applied

### Fix 1 — Created `AuthService` as a Reactive Single Source of Truth

A new `AuthService` was created using a **`BehaviorSubject`**. All reads/writes to
the citizen session now go through this service:

```typescript
// auth.service.ts (NEW)
@Injectable({ providedIn: 'root' })
export class AuthService {
  private citizenIdSubject = new BehaviorSubject<string>(
    localStorage.getItem('citizenId') || ''
  );

  citizenId$ = this.citizenIdSubject.asObservable();
  get citizenId(): string { return this.citizenIdSubject.getValue(); }

  setCitizenId(id: string, fullName?: string): void {
    localStorage.setItem('citizenId', id);
    if (fullName) localStorage.setItem('citizenName', fullName);
    this.citizenIdSubject.next(id);   // ← notifies ALL subscribers instantly
  }
}
```

### Fix 2 — `HeaderComponent` Subscribes to `citizenId$`

Instead of a one-time read, the header now **reactively updates** whenever the ID changes:

```typescript
// header.component.ts — AFTER (fixed)
ngOnInit() {
  this.authSub = this.authService.citizenId$.subscribe(id => {
    this.citizenId = id;   // ← updated every time a new citizen registers/logs in
  });
}

ngOnDestroy() {
  this.authSub?.unsubscribe();
}
```

### Fix 3 — `RegisterComponent` Uses `AuthService.setCitizenId()`

```typescript
// register.component.ts — AFTER (fixed)
// Instead of direct localStorage writes:
this.authService.setCitizenId(res.data.id, res.data.fullName);
// This writes to localStorage AND broadcasts to all subscribers simultaneously.
```

### Fix 4 — `HomeComponent` Uses `AuthService` for Consistency

```typescript
// home.component.ts — AFTER (fixed)
ngOnInit() {
  this.citizenId = this.authService.citizenId;
}
```

---

## 📁 Files Changed

| File | Type | Change |
|------|------|--------|
| `frontend/src/app/services/auth.service.ts` | **NEW** | Reactive session service with `BehaviorSubject` |
| `frontend/src/app/shared/header/header.component.ts` | MODIFIED | Subscribes to `AuthService.citizenId$` |
| `frontend/src/app/components/register/register.component.ts` | MODIFIED | Uses `authService.setCitizenId()` |
| `frontend/src/app/components/home/home.component.ts` | MODIFIED | Reads from `AuthService` |

---

## 🔁 How to Reproduce (Before Fix)

1. Open app in browser — no session exists
2. Register as `test41` → `citizenId = 62b16bad-...` is stored, header updates ✅
3. **Without refreshing**, register again as `test46` → `citizenId = 37f359f5-...` is stored
4. Home page shows `test46` correctly (component re-created on navigation)
5. Click **Profile** in navbar → navigates to `test41`'s profile ❌
   - Because the header still holds the stale `62b16bad-...` from step 2

---

## ✔️ Verification (After Fix)

1. Register as any citizen → header Profile link updates **immediately** to new ID
2. Home page "View My Profile" and navbar "Profile" now point to the **same** citizen
3. No page refresh required

---

## 💡 Lessons Learned

| # | Lesson |
|---|--------|
| 1 | Angular persistent components (header, sidebar) must **never** rely on `ngOnInit` for data that can change during the app's lifetime — use reactive patterns (`BehaviorSubject`, `Signal`). |
| 2 | Use a **single service** as the source of truth for session state; never scatter `localStorage.getItem/setItem` calls across components. |
| 3 | `localStorage` writes do not emit Angular change-detection events — subscribing to a `BehaviorSubject` is the correct way to propagate state changes reactively. |

---

## 🔗 Related Issue — ISSUE-001: OTP Verification Fails with Kafka TimeoutException

> This issue occurred in the **same session** and contributed to the same overall symptom
> of incorrect/broken user data flows in the Citizen Services Portal.

| Field         | Detail                                      |
|---------------|---------------------------------------------|
| **Serial No** | ISSUE-001                                   |
| **Date**      | 2026-07-15                                  |
| **Service**   | `ekyc-service` (port 8082)                  |
| **Severity**  | 🔴 Critical — OTP verification completely broken |
| **Status**    | ✅ Resolved                                  |

### What Happened

When a citizen entered OTP `123456` on the eKYC Verification page and clicked
**"Verify OTP"**, the backend `ekyc-service` crashed with a `500 Internal Server Error`
instead of returning `KycStatus.VERIFIED`.

```
Frontend:
  POST http://localhost:8082/api/kyc/verify-otp  500 (Internal Server Error)

Backend (ekyc-service):
  org.springframework.kafka.KafkaException: Send failed
  caused by: org.apache.kafka.common.errors.TimeoutException:
    Topic kyc.verification.completed not present in metadata after 60000 ms.
```

### Two Root Causes

**Bug A — Wrong Kafka Bootstrap Port in `KafkaConfig.java`:**

```java
// ❌ BEFORE — hardcoded wrong port (broker unreachable from Java)
configProps.put("bootstrap.servers", "localhost:9092");

// ✅ AFTER — correct port matching docker-compose.yml
configProps.put("bootstrap.servers", "localhost:39092");
```

The Docker Compose exposes Kafka on `localhost:39092`, not `9092`. `application.yaml`
had the correct value `${KAFKA_BOOTSTRAP_SERVERS:localhost:39092}` but `KafkaConfig.java`
bypassed it entirely by hardcoding the port in Java.

**Bug B — Kafka Topics Not Created:**

The `kafka-init` container (a one-shot Docker init container) had already exited and the
topics `kyc.verification.completed` and `kyc.initiation.event` were never persisted
across a Docker restart.

```bash
# Confirmed only __consumer_offsets existed — neither KYC topic was present
docker exec kafka kafka-topics --bootstrap-server kafka:29092 --list
```

### Fixes Applied

| Fix | Action |
|-----|--------|
| Fix 1 | `KafkaConfig.java` — corrected `bootstrap.servers` from `9092` → `39092` in both `producerFactory` and `consumerFactory` |
| Fix 2 | Manually created missing topics via `docker exec kafka kafka-topics --bootstrap-server kafka:29092 --create ...` |
| Fix 3 | Added `KafkaAdmin` + `NewTopic` `@Bean`s to `KafkaConfig.java` so topics auto-create on every service startup |

### Files Changed

| File | Change |
|------|--------|
| `ekyc-service/.../config/KafkaConfig.java` | Fixed port; added `KafkaAdmin`, `kycVerificationCompletedTopic`, `kycInitiationEventTopic` beans |

### Connection to ISSUE-002

Both issues occurred in the same user session and share the same root theme:
**infrastructure/state not updated to reflect the currently active citizen**.
- ISSUE-001: the Kafka broker/topics were not correctly configured → KYC verification
  failed → citizen's KYC status was stuck in a broken state.
- ISSUE-002: the Angular header held a stale citizenId → the wrong citizen's profile
  was shown in the navbar.

> See [ISSUE_001.md](./ISSUE_001.md) for the standalone full write-up of the Kafka issue.
