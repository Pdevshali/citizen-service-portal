-- ─────────────────────────────────────────────────────────────────────────────
-- V0__Base_schema.sql
-- The ORIGINAL schema — citizens and documents tables as they existed
-- before keycloak_user_id was introduced (that is added by V2).
-- V1 fixes the documents check constraint.
-- V2 adds keycloak_user_id to citizens.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS citizens (
    id                          VARCHAR(36)     PRIMARY KEY,          -- UUID
    full_name                   VARCHAR(255)    NOT NULL,
    email                       VARCHAR(255)    NOT NULL UNIQUE,
    phone                       VARCHAR(255)    NOT NULL UNIQUE,
    date_of_birth               DATE,
    aadhaar_number              VARCHAR(255)    UNIQUE,
    address                     VARCHAR(255),
    state                       VARCHAR(255),
    pincode                     VARCHAR(6),
    kyc_status                  VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    kyc_initiated_at            TIMESTAMP,
    kyc_verified_at             TIMESTAMP,
    demographic_data_encrypted  TEXT,
    registered_at               TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP
    -- NOTE: keycloak_user_id is NOT here — it is added by V2
);

CREATE TABLE IF NOT EXISTS documents (
    id              VARCHAR(36)     PRIMARY KEY,                       -- UUID
    citizen_id      VARCHAR(36)     NOT NULL REFERENCES citizens(id),
    document_type   VARCHAR(50)     NOT NULL,
    document_url    VARCHAR(255)    NOT NULL,
    fetched_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT documents_document_type_check
        CHECK (document_type IN ('AADHAAR', 'PAN', 'PASSPORT'))       -- V1 adds CERTIFICATE
);
