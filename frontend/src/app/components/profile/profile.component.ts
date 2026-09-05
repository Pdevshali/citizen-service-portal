import { Component } from '@angular/core';
import { DatePipe } from '@angular/common';
import { CitizenService } from '../../services/citizen.service';
import { CitizenProfileResponse } from '../../models/models';

@Component({
  selector: 'app-profile',
  imports: [DatePipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  profile: CitizenProfileResponse | null = null;
  loading = true;
  error = '';

  constructor(
    private citizenService: CitizenService
  ) {}

  ngOnInit() {
    this.citizenService.getMe().subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.profile = res.data;
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to load profile.';
      }
    });
  }

  getKycBadgeClass(): string {
    if (!this.profile) return '';
    const map: Record<string, string> = {
      VERIFIED: 'badge-success',
      INITIATED: 'badge-warning',
      PENDING: 'badge-info',
      FAILED: 'badge-danger'
    };
    return map[this.profile.kycStatus] || '';
  }
}
