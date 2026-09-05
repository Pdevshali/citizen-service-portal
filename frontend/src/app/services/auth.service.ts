import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';

/**
 * AuthService — single source of truth for authentication state.
 *
 * Authentication is driven entirely by Keycloak (JWT). The service exposes
 * helpers that wrap KeycloakService so components never import
 * KeycloakService directly, keeping them easy to test and swap.
 *
 * The `citizenId` / `citizenName` properties are purely non-security UI data
 * (e.g., to build a "Welcome, John" greeting). They are NOT used for
 * authorisation — the backend derives ownership from the JWT sub claim.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  // ── Non-security UI state (portal citizenId, not a security token) ─────────
  private citizenIdSubject = new BehaviorSubject<string>(
    sessionStorage.getItem('citizenId') || ''
  );

  /** Observable stream of the portal citizenId (UI data only). */
  citizenId$ = this.citizenIdSubject.asObservable();

  /** Synchronous snapshot of the portal citizenId. */
  get citizenId(): string {
    return this.citizenIdSubject.getValue();
  }

  constructor(private keycloak: KeycloakService) {}

  // ── Keycloak authentication methods ────────────────────────────────────────

  /** Returns true when the user has an active Keycloak session. */
  isLoggedIn(): boolean {
    return this.keycloak.isLoggedIn();
  }

  /**
   * Redirects the browser to the Keycloak login page.
   * On success Keycloak redirects back to the current page (or redirectUri).
   */
  login(redirectUri?: string): void {
    this.keycloak.login({
      redirectUri: redirectUri ?? window.location.origin + '/onboarding',
    });
  }

  /**
   * Redirects the browser to the Keycloak self-registration page.
   * Angular never handles passwords; Keycloak does.
   * After successful registration Keycloak redirects to /onboarding.
   */
  register(): void {
    this.keycloak.register({
      redirectUri: window.location.origin + '/onboarding',
    });
  }

  /**
   * Terminates the Keycloak session and clears all local UI state.
   * Keycloak redirects back to the home page after logout.
   */
  logout(): void {
    this.clearSession();
    this.keycloak.logout(window.location.origin);
  }

  /**
   * Returns the Keycloak user profile (displayName, email, etc.).
   * Returns null if not logged in.
   */
  async getUserProfile(): Promise<{ username?: string; email?: string; firstName?: string } | null> {
    if (!this.isLoggedIn()) return null;
    try {
      return await this.keycloak.loadUserProfile();
    } catch {
      return null;
    }
  }

  // ── Non-security portal citizen data ──────────────────────────────────────

  /**
   * Call after a successful onboarding to store the portal citizenId for UI use.
   * Uses sessionStorage (not localStorage) — cleared when browser tab closes.
   */
  setCitizenId(id: string, fullName?: string): void {
    sessionStorage.setItem('citizenId', id);
    if (fullName) {
      sessionStorage.setItem('citizenName', fullName);
    }
    this.citizenIdSubject.next(id);
  }

  /** Clears portal UI state (does NOT log the user out of Keycloak). */
  clearSession(): void {
    sessionStorage.removeItem('citizenId');
    sessionStorage.removeItem('citizenName');
    this.citizenIdSubject.next('');
  }
}
