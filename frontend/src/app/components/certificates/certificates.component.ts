import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { CertificateRequest } from '../../models/models';

@Component({
  selector: 'app-certificates',
  imports: [FormsModule, RouterLink],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.css'
})
export class CertificatesComponent {
  citizenId = localStorage.getItem('citizenId') || '';
  certificateType = '';
  purpose = '';
  remarks = '';
  loading = false;
  error = '';
  success = '';

  constructor(private citizenService: CitizenService) {}

  onSubmit() {
    if (!this.citizenId || !this.certificateType || !this.purpose) return;
    this.loading = true;
    this.error = '';
    this.success = '';
    const request: CertificateRequest = {
      certificateType: this.certificateType,
      purpose: this.purpose,
      remarks: this.remarks || undefined
    };
    this.citizenService.requestCertificate(this.citizenId, request).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.success = `Certificate request for "${this.certificateType}" has been submitted successfully.`;
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to submit certificate request.';
      }
    });
  }
}
