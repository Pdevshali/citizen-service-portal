import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { DocumentFetchRequest, DocumentType } from '../../models/models';

@Component({
  selector: 'app-documents',
  imports: [FormsModule, RouterLink],
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.css'
})
export class DocumentsComponent {
  citizenId = localStorage.getItem('citizenId') || '';
  aadhaar = '';
  documentType: DocumentType = 'AADHAAR';
  loading = false;
  error = '';
  success = '';

  constructor(private citizenService: CitizenService) {}

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
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to request document fetch.';
      }
    });
  }
}
