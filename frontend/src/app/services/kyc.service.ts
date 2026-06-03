import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, GenerateOtpRequest, GenerateOtpResponse, VerifyOtpRequest, VerifyOtpResponse, KycStatusResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class KycService {
  private baseUrl = 'http://localhost:8082/api/kyc';

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
