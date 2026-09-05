import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { KycService } from '../../services/kyc.service';
import { AuthService } from '../../services/auth.service';
import { KycInitiateRequest, GenerateOtpRequest, VerifyOtpRequest, KycStatusResponse } from '../../models/models';

@Component({
  selector: 'app-kyc',
  imports: [FormsModule, RouterLink],
  templateUrl: './kyc.component.html',
  styleUrl: './kyc.component.css'
})
export class KycComponent {
  aadhaar = '';
  txnId = '';
  otp = '';
  step: 'initiate' | 'otp' | 'status' = 'initiate';
  loading = false;
  error = '';
  success = '';
  kycStatus: KycStatusResponse | null = null;

  constructor(
    private citizenService: CitizenService,
    private kycService: KycService,
    private authService: AuthService
  ) {}

  /** Current portal citizen ID managed by AuthService/sessionStorage. */
  get citizenId(): string {
    return this.authService.citizenId;
  }

  onInitiateKyc() {
    if (!this.citizenId || !this.aadhaar) return;
    this.loading = true;
    this.error = '';
    const request: KycInitiateRequest = {
      citizenId: this.citizenId,
      aadhaarNumber: this.aadhaar,
      phone: this.aadhaar ? undefined : undefined
    };
    this.citizenService.initiateKyc(request).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.step = 'otp';
          this.success = 'KYC initiated! OTP has been sent to your registered mobile.';
          setTimeout(() => this.generateOtp(), 500);
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to initiate KYC.';
      }
    });
  }

  generateOtp() {
    const request: GenerateOtpRequest = {
      citizenId: this.citizenId,
      aadhaarNumber: this.aadhaar
    };
    this.kycService.generateOtp(request).subscribe({
      next: (res) => {
        if (res.success) {
          this.txnId = res.data.txnId;
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to generate OTP.';
      }
    });
  }

  onVerifyOtp() {
    if (!this.txnId || !this.otp) return;
    this.loading = true;
    this.error = '';
    const request: VerifyOtpRequest = {
      txnId: this.txnId,
      otp: this.otp
    };
    this.kycService.verifyOtp(request).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.success = 'KYC verification successful!';
          this.step = 'status';
          this.loadStatus();
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'OTP verification failed.';
      }
    });
  }

  loadStatus() {
    this.kycService.getStatus(this.citizenId).subscribe({
      next: (res) => {
        if (res.success) {
          this.kycStatus = res.data;
        }
      }
    });
  }

  checkStatus() {
    this.step = 'status';
    this.loadStatus();
  }
}
