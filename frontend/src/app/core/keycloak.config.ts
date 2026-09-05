/**
 * Keycloak configuration for the Citizen Services Portal.
 *
 * All Keycloak settings are kept in one place for easy environment-specific overrides.
 * In production, replace these with environment-specific values via CI/CD injection.
 */
export const keycloakConfig = {
  /** Keycloak server URL — must match KC_HTTP_PORT in docker-compose */
  url: 'http://localhost:8180',

  /** Realm configured in Keycloak admin console */
  realm: 'citizen-portal',

  /**
   * Client ID configured in Keycloak.
   * Should match what's set in the Keycloak admin console under Clients.
   * Access type: public (SPA — no client secret needed)
   */
  clientId: 'citizen-portal-client',
};

/** Gateway base URL — all API calls go through here, not directly to microservices */
export const apiGatewayUrl = 'http://localhost:8080';
