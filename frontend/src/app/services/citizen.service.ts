import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, CitizenRegistrationRequest, CitizenProfileResponse, KycInitiateRequest, DocumentFetchRequest, CertificateRequest, ServiceRequestResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CitizenService {
  private baseUrl = 'http://localhost:8081/api/citizens';

  constructor(private http: HttpClient) {}

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
