import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';

/**
 * Route guard that protects routes requiring authentication.
 *
 * Extends {@link KeycloakAuthGuard} which handles the Keycloak-specific
 * authentication check. If the user is not authenticated, they are
 * redirected to the Keycloak login page.
 *
 * Usage in routes:
 * ```ts
 * { path: 'kyc', component: KycComponent, canActivate: [AuthGuard] }
 * ```
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected override readonly router: Router,
    protected readonly keycloak: KeycloakService
  ) {
    super(router, keycloak);
  }

  /**
   * Called by KeycloakAuthGuard with the authentication state.
   * Return true to allow navigation, false to block it.
   *
   * @param route - the activated route snapshot
   * @param state - the router state snapshot
   */
  public async isAccessAllowed(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {

    // If user is not authenticated, redirect to Keycloak login page
    if (!this.authenticated) {
      await this.keycloak.login({
        redirectUri: window.location.origin + state.url,
      });
      return false;
    }

    // Check required roles if specified in route data
    const requiredRoles = route.data?.['roles'] as string[] | undefined;
    if (!requiredRoles || requiredRoles.length === 0) {
      return true; // No role requirement — just authentication is enough
    }

    // Verify the user has at least one of the required roles
    const userRoles = this.roles;
    const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role));

    if (!hasRequiredRole) {
      // User is authenticated but lacks required role — redirect to home
      await this.router.navigate(['/']);
    }

    return hasRequiredRole;
  }
}
