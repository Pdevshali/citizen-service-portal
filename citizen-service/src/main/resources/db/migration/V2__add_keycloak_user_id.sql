-- Migration: Link Citizen records to Keycloak accounts
-- Adds a keycloak_user_id column so each citizen row can be tied
-- to the Keycloak subject (sub claim) of the authenticated user.
--
-- Initially nullable so existing rows are unaffected.
-- Once all existing citizens complete the onboarding flow this
-- can be tightened to NOT NULL in a future migration.

ALTER TABLE citizens
    ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(36);

-- Enforce uniqueness: one Keycloak account → one citizen profile
ALTER TABLE citizens
    ADD CONSTRAINT uq_citizens_keycloak_user_id UNIQUE (keycloak_user_id);

-- Speed up the lookup performed by GET /api/citizens/me
CREATE INDEX IF NOT EXISTS idx_citizens_keycloak_user_id
    ON citizens (keycloak_user_id);
