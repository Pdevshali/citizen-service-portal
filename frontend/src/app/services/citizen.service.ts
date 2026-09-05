import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  CitizenRegistrationRequest,
  CitizenProfileResponse,
  KycInitiateRequest,
  DocumentFetchRequest,
  CertificateRequest,
  ServiceRequestResponse,
  OnboardingRequest
} from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

/**
 * Service for citizen-related API calls.
 * Routed through API Gateway — Bearer token auto-attached by KeycloakBearerInterceptor.
 *
 * New authenticated endpoints:
 *   getMe()    → GET  /api/citizens/me
 *   onboard()  → POST /api/citizens/me/onboarding
 *
 * Legacy endpoints (still functional, frontend no longer calls them directly):
 *   register() → POST /api/citizens/register  (public, no JWT needed)
 */
@Injectable({ providedIn: 'root' })
export class CitizenService {
  /** Route through API Gateway instead of directly to citizen-service:8081 */
  private baseUrl = `${apiGatewayUrl}/api/citizens`;

  constructor(private http: HttpClient) {}

  // ── Keycloak-authenticated "current user" endpoints ──────────────────────

  /**
   * GET /api/citizens/me
   * Returns the citizen profile for the currently logged-in Keycloak user.
   * Returns 404 when the user hasn't completed onboarding yet — Angular
   * uses that signal to redirect to /onboarding.
   */
  getMe(): Observable<ApiResponse<CitizenProfileResponse>> {
    return this.http.get<ApiResponse<CitizenProfileResponse>>(`${this.baseUrl}/me`);
  }

  /**
   * POST /api/citizens/me/onboarding
   * Creates the citizen profile for the currently logged-in Keycloak user.
   * email and keycloakUserId are derived from the JWT on the backend.
   */
  onboard(request: OnboardingRequest): Observable<ApiResponse<CitizenProfileResponse>> {
    return this.http.post<ApiResponse<CitizenProfileResponse>>(`${this.baseUrl}/me/onboarding`, request);
  }

  // ── Legacy / admin endpoints ─────────────────────────────────────────────

  register(request: CitizenRegistrationRequest): Observable<ApiResponse<CitizenProfileResponse>> {
    return this.http.post<ApiResponse<CitizenProfileResponse>>(`${this.baseUrl}/register`, request);
  }

  validate(id: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${id}/validate`);
  }

  getProfile(id: string): Observable<ApiResponse<CitizenProfileResponse>> {
    return this.http.get<ApiResponse<CitizenProfileResponse>>(`${this.baseUrl}/${id}/profile`);
  }

  initiateKyc(request: KycInitiateRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/initiate-kyc`, request);
  }

  fetchDocument(request: DocumentFetchRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/fetch-document`, request);
  }

  requestCertificate(id: string, request: CertificateRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/${id}/request-certificate`, request);
  }

  getServices(id: string): Observable<ApiResponse<ServiceRequestResponse[]>> {
    return this.http.get<ApiResponse<ServiceRequestResponse[]>>(`${this.baseUrl}/${id}/services`);
  }
}

