import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, CertificateResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CertificateService {
  private baseUrl = 'http://localhost:8084/api/certificates';

  constructor(private http: HttpClient) {}

  listCertificates(citizenId: string): Observable<ApiResponse<CertificateResponse[]>> {
    return this.http.get<ApiResponse<CertificateResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }
}
