import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { CertificateService } from '../../services/certificate.service';
import { AuthService } from '../../services/auth.service';
import { CertificateRequest, CertificateResponse } from '../../models/models';

@Component({
  selector: 'app-certificates',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.css'
})
export class CertificatesComponent {
  // Onboarding stores the portal ID through AuthService/sessionStorage.
  // Reading localStorage here incorrectly displayed the registration prompt.
  citizenId = '';
  certificateType = '';
  purpose = '';
  remarks = '';
  loading = false;
  certificatesLoading = false;
  error = '';
  success = '';
  certificates: CertificateResponse[] = [];

  constructor(
    private citizenService: CitizenService,
    private certificateService: CertificateService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.citizenId = this.authService.citizenId;
    this.loadCertificates();
  }

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
          this.loadCertificates();
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to submit certificate request.';
      }
    });
  }

  loadCertificates() {
    if (!this.citizenId) return;
    this.certificatesLoading = true;
    this.certificateService.listCertificates(this.citizenId).subscribe({
      next: (res) => {
        this.certificatesLoading = false;
        if (res.success) {
          this.certificates = res.data;
        }
      },
      error: (err) => {
        this.certificatesLoading = false;
        this.error = err.error?.message || 'Failed to load certificates.';
      }
    });
  }

  statusClass(status: string): string {
    return `status ${status.toLowerCase()}`;
  }
}
