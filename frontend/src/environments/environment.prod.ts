/**
 * Production environment configuration for Vercel deployment.
 *
 * Values are injected via Vercel Environment Variables at build time.
 * Set these in your Vercel project dashboard → Settings → Environment Variables:
 *
 *   KEYCLOAK_URL         → public EC2 Keycloak URL  e.g. http://<ec2-ip>:8180
 *   KEYCLOAK_REALM       → citizen-portal  (or your realm name)
 *   KEYCLOAK_CLIENT_ID   → citizen-portal-client
 *   API_GATEWAY_URL      → public EC2 API Gateway URL  e.g. http://<ec2-ip>:8080
 *
 * The build script (scripts/set-env.js) reads these Node env vars and
 * writes them into this file before `ng build --configuration production` runs.
 *
 * ⚠️  Do NOT hardcode real IPs/secrets here — always use Vercel env vars.
 */
export const environment = {
  production: true,
  keycloak: {
    url: 'KEYCLOAK_URL_PLACEHOLDER',
    realm: 'KEYCLOAK_REALM_PLACEHOLDER',
    clientId: 'KEYCLOAK_CLIENT_ID_PLACEHOLDER',
  },
  apiGatewayUrl: 'API_GATEWAY_URL_PLACEHOLDER',
};

