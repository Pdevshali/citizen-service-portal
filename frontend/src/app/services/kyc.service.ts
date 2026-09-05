import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, GenerateOtpRequest, GenerateOtpResponse, VerifyOtpRequest, VerifyOtpResponse, KycStatusResponse } from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

/**
 * Service for eKYC-related API calls.
 * Routed through API Gateway — Bearer token auto-attached by KeycloakBearerInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class KycService {
  /** Route through API Gateway instead of directly to ekyc-service:8082 */
  private baseUrl = `${apiGatewayUrl}/api/kyc`;

  constructor(private http: HttpClient) {}

  generateOtp(request: GenerateOtpRequest): Observable<ApiResponse<GenerateOtpResponse>> {
    return this.http.post<ApiResponse<GenerateOtpResponse>>(`${this.baseUrl}/generate-otp`, request);
  }

  verifyOtp(request: VerifyOtpRequest): Observable<ApiResponse<VerifyOtpResponse>> {
    return this.http.post<ApiResponse<VerifyOtpResponse>>(`${this.baseUrl}/verify-otp`, request);
  }

  getStatus(citizenId: string): Observable<ApiResponse<KycStatusResponse>> {
    return this.http.get<ApiResponse<KycStatusResponse>>(`${this.baseUrl}/${citizenId}/status`);
  }
}
