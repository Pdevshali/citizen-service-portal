import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { DocumentService } from '../../services/document.service';
import { AuthService } from '../../services/auth.service';
import { DocumentFetchRequest, DocumentResponse, DocumentType } from '../../models/models';

@Component({
  selector: 'app-documents',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.css'
})
export class DocumentsComponent {
  // Keep the portal ID in sync with the authenticated session.  Onboarding
  // stores this value through AuthService/sessionStorage; reading localStorage
  // here made registered users appear unregistered.
  citizenId = '';
  aadhaar = '';
  documentType: DocumentType = 'AADHAAR';
  loading = false;
  documentsLoading = false;
  error = '';
  success = '';
  documents: DocumentResponse[] = [];

  constructor(
    private citizenService: CitizenService,
    private documentService: DocumentService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.citizenId = this.authService.citizenId;
    this.loadDocuments();
  }

  onSubmit() {
    if (!this.citizenId) return;
    this.loading = true;
    this.error = '';
    this.success = '';
    const request: DocumentFetchRequest = {
      citizenId: this.citizenId,
      aadhaarNumber: this.aadhaar || undefined,
      documentType: this.documentType
    };
    this.citizenService.fetchDocument(request).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.success = `Document fetch request for ${this.documentType} has been submitted. You will be notified once it's ready.`;
          this.loadDocuments();
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to request document fetch.';
      }
    });
  }

  loadDocuments() {
    if (!this.citizenId) return;
    this.documentsLoading = true;
    this.documentService.listDocuments(this.citizenId).subscribe({
      next: (res) => {
        this.documentsLoading = false;
        if (res.success) {
          this.documents = res.data;
        }
      },
      error: (err) => {
        this.documentsLoading = false;
        this.error = err.error?.message || 'Failed to load documents.';
      }
    });
  }

  statusClass(status: string): string {
    return `status ${status.toLowerCase().replace('_', '-')}`;
  }
}
