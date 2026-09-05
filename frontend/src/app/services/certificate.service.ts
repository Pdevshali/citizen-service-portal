import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, CertificateResponse } from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

/**
 * Service for certificate-related API calls.
 * Routed through API Gateway — Bearer token auto-attached by KeycloakBearerInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class CertificateService {
  /** Route through API Gateway instead of directly to certificate-service:8084 */
  private baseUrl = `${apiGatewayUrl}/api/certificates`;

  constructor(private http: HttpClient) {}

  listCertificates(citizenId: string): Observable<ApiResponse<CertificateResponse[]>> {
    return this.http.get<ApiResponse<CertificateResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }
}
