import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/**
 * AuthService — single source of truth for the currently logged-in citizen.
 *
 * All components that need the citizenId should inject this service and
 * subscribe to citizenId$ (or read citizenId snapshot) instead of calling
 * localStorage.getItem('citizenId') directly.  This ensures every part of
 * the UI (including the persistent header) reacts immediately when a new
 * registration replaces the stored id.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private citizenIdSubject = new BehaviorSubject<string>(
    localStorage.getItem('citizenId') || ''
  );

  /** Observable stream — subscribe to always have the current citizenId. */
  citizenId$ = this.citizenIdSubject.asObservable();

  /** Synchronous snapshot of the current citizenId. */
  get citizenId(): string {
    return this.citizenIdSubject.getValue();
  }

  /**
   * Call this after a successful registration or login.
   * Persists to localStorage AND notifies all subscribers.
   */
  setCitizenId(id: string, fullName?: string): void {
    localStorage.setItem('citizenId', id);
    if (fullName) {
      localStorage.setItem('citizenName', fullName);
    }
    this.citizenIdSubject.next(id);
  }

  /** Call on logout to clear the session. */
  clearSession(): void {
    localStorage.removeItem('citizenId');
    localStorage.removeItem('citizenName');
    this.citizenIdSubject.next('');
  }
}
