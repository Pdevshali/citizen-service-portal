import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, GrievanceSubmitRequest, GrievanceResponse } from '../models/models';
import { apiGatewayUrl } from '../core/keycloak.config';

@Injectable({ providedIn: 'root' })
export class GrievanceService {
  private baseUrl = `${apiGatewayUrl}/api/grievances`;

  constructor(private http: HttpClient) {}

  submitGrievance(request: GrievanceSubmitRequest): Observable<ApiResponse<GrievanceResponse>> {
    return this.http.post<ApiResponse<GrievanceResponse>>(`${this.baseUrl}/submit`, request);
  }

  // New: fetch grievances for the authenticated user (server derives citizenId from JWT)
  getMyGrievances(): Observable<ApiResponse<GrievanceResponse[]>> {
    return this.http.get<ApiResponse<GrievanceResponse[]>>(`${this.baseUrl}/me`);
  }

  listByCitizen(citizenId: string): Observable<ApiResponse<GrievanceResponse[]>> {
    return this.http.get<ApiResponse<GrievanceResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }

  getGrievance(id: string): Observable<ApiResponse<GrievanceResponse>> {
    return this.http.get<ApiResponse<GrievanceResponse>>(`${this.baseUrl}/${id}`);
  }
}
