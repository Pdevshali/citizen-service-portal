import { KeycloakService } from 'keycloak-angular';
import { keycloakConfig } from './keycloak.config';

/**
 * Keycloak initialization factory.
 *
 * This factory is used as an APP_INITIALIZER so that Keycloak is fully
 * initialized before the Angular application bootstraps. This ensures
 * that the auth state is known before any route guard or component runs.
 *
 * @param keycloak - the injected KeycloakService
 * @returns a factory function that returns a Promise<boolean>
 */
export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () =>
    keycloak
      .init({
        config: keycloakConfig,
        initOptions: {
          /**
           * 'check-sso': silently checks for an existing SSO session.
           * The user stays on the page if not logged in (no redirect).
           *
           * silentCheckSsoRedirectUri is intentionally omitted here because
           * modern browsers block third-party cookies inside cross-origin iframes,
           * which causes a timeout error and a blank page in production (Vercel).
           * Without it, Keycloak falls back to a safe non-iframe flow.
           */
          onLoad: 'check-sso',

          /**
           * Disable the login-iframe polling entirely.
           * The iframe approach is unreliable in production (third-party cookie restrictions).
           */
          checkLoginIframe: false,
        },
        /** Automatically add Bearer token to all HTTP requests */
        enableBearerInterceptor: true,

        /**
         * URL patterns that should receive the Bearer token.
         * The gateway URL pattern ensures all API calls through the gateway are authenticated.
         * Public endpoints (like /register) will still have the token sent — the gateway's
         * permitAll() configuration simply won't enforce it.
         */
        bearerExcludedUrls: [],
      })
      .catch((err) => {
        /**
         * If Keycloak init fails (e.g. server unreachable, CORS error),
         * log the error but resolve so Angular still bootstraps.
         * The app will run in an unauthenticated state — guards will redirect
         * to login when a protected route is accessed.
         */
        console.error('[Keycloak] Initialization failed:', err);
        return false;
      });
}
