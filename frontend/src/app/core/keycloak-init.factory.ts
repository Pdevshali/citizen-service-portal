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
    keycloak.init({
      config: keycloakConfig,
      initOptions: {
        /**
         * 'check-sso': The adapter will try to authenticate the user silently.
         * If the user is already logged in (SSO session exists), they'll be authenticated
         * automatically. If not, the user stays on the current page without being redirected.
         * Use 'login-required' to always force a redirect to the login page.
         */
        onLoad: 'check-sso',

        /**
         * Enables the silent SSO check using an iframe at this URL.
         * Create the file at: src/assets/silent-check-sso.html
         */
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',

        /** Disable pkce flow for development simplicity (enable in production) */
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
    });
}
