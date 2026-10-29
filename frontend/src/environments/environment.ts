/**
 * Development environment configuration.
 * These values are used when running `ng serve` locally.
 * In production (Vercel), environment.prod.ts is used instead.
 */
export const environment = {
  production: false,
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'citizen-portal',
    clientId: 'citizen-portal-client',
  },
  apiGatewayUrl: 'http://localhost:8080',
};

