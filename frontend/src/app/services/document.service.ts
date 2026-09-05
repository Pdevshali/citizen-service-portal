import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, DocumentResponse } from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

/**
 * Service for document-related API calls.
 * All calls go through the API Gateway (port 8080), which handles JWT validation.
 * The Bearer token is automatically injected by the KeycloakBearerInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class DocumentService {
  /** Route through API Gateway instead of directly to document-service:8083 */
  private baseUrl = `${apiGatewayUrl}/api/documents`;

  constructor(private http: HttpClient) {}

  listDocuments(citizenId: string): Observable<ApiResponse<DocumentResponse[]>> {
    return this.http.get<ApiResponse<DocumentResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }
}
