import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, DocumentResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private baseUrl = 'http://localhost:8083/api/documents';

  constructor(private http: HttpClient) {}

  listDocuments(citizenId: string): Observable<ApiResponse<DocumentResponse[]>> {
    return this.http.get<ApiResponse<DocumentResponse[]>>(`${this.baseUrl}/citizen/${citizenId}`);
  }
}
