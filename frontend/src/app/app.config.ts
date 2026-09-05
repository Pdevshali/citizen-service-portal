import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { KeycloakAngularModule, KeycloakBearerInterceptor, KeycloakService } from 'keycloak-angular';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import { initializeKeycloak } from './core/keycloak-init.factory';

/**
 * Root application configuration.
 *
 * Registers Keycloak as an APP_INITIALIZER so it initializes before the app
 * bootstraps. The KeycloakBearerInterceptor automatically attaches the JWT
 * Bearer token to all outgoing HTTP requests.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),

    // Use withInterceptorsFromDi so the Keycloak interceptor (DI-based) is picked up
    provideHttpClient(withInterceptorsFromDi()),

    // ── Keycloak Service ───────────────────────────────────────────────────────
    KeycloakService,

    // ── Keycloak Initialization — runs before app bootstraps ──────────────────
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },

    // ── Bearer Token Interceptor — auto-attaches JWT to HTTP requests ─────────
    {
      provide: HTTP_INTERCEPTORS,
      useClass: KeycloakBearerInterceptor,
      multi: true,
    },
  ],
};
