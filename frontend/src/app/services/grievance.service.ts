import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  GrievanceSubmitRequest,
  GrievanceResponse,
  GrievanceSearchParams,
  PageResponse,
} from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

@Injectable({ providedIn: 'root' })
export class GrievanceService {
  private baseUrl = `${apiGatewayUrl}/api/grievances`;

  constructor(private http: HttpClient) {}

  submitGrievance(request: GrievanceSubmitRequest): Observable<ApiResponse<GrievanceResponse>> {
    return this.http.post<ApiResponse<GrievanceResponse>>(`${this.baseUrl}/submit`, request);
  }

  // New unified search API — replaces getMyGrievances / listByCitizen in the frontend.
  // Backend enforces citizenId = JWT subject for CITIZEN role automatically.
  searchGrievances(params: GrievanceSearchParams): Observable<ApiResponse<PageResponse<GrievanceResponse>>> {
    const httpParams = new HttpParams({ fromObject: this.toStringRecord(params) });
    return this.http.get<ApiResponse<PageResponse<GrievanceResponse>>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }

  // ── Legacy methods kept for backward compatibility (no longer used in UI) ──

  /** @deprecated Use searchGrievances() instead */
  getMyGrievances(): Observable<ApiResponse<GrievanceResponse[]>> {
    return this.http.get<ApiResponse<GrievanceResponse[]>>(`${this.baseUrl}/me`);
  }

  /** @deprecated Use searchGrievances({ citizenId }) instead */
  listByCitizen(citizenId: string): Observable<ApiResponse<GrievanceResponse[]>> {
    return this.http.get<ApiResponse<GrievanceResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }

  getGrievance(id: string): Observable<ApiResponse<GrievanceResponse>> {
    return this.http.get<ApiResponse<GrievanceResponse>>(`${this.baseUrl}/${id}`);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  /** Removes null / undefined / empty-string entries so HttpParams stays clean */
  private toStringRecord(params: GrievanceSearchParams): Record<string, string> {
    return Object.fromEntries(
      Object.entries(params)
        .filter(([, v]) => v !== null && v !== undefined && v !== '')
        .map(([k, v]) => [k, String(v)])
    );
  }
}
