import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { AuthService } from '../../services/auth.service';
import { OnboardingRequest } from '../../models/models';

/**
 * OnboardingComponent — shown once after a new user logs in via Keycloak.
 *
 * Flow:
 * 1. On init, call GET /api/citizens/me.
 *    • 200 → user already onboarded → redirect to /profile
 *    • 404 → user not yet onboarded → show profile form
 * 2. User fills in the form (no email/password — those are in Keycloak).
 * 3. POST /api/citizens/me/onboarding → backend links citizen row to Keycloak sub.
 * 4. Redirect to /profile.
 */
@Component({
  selector: 'app-onboarding',
  imports: [FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.css'
})
export class OnboardingComponent implements OnInit {

  model: OnboardingRequest = {
    fullName: '',
    phone: '',
    dateOfBirth: '',
    aadhaarNumber: '',
    address: '',
    state: '',
    pincode: ''
  };

  loading = true;          // true while checking /me on init
  submitting = false;      // true while POST /me/onboarding is in flight
  error = '';
  checkingMe = true;       // shows a "Checking your profile..." spinner

  constructor(
    private citizenService: CitizenService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Check whether the authenticated user already has a citizen profile.
    this.citizenService.getMe().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          // Already onboarded — store citizenId and go straight to profile
          this.authService.setCitizenId(res.data.id, res.data.fullName);
          this.router.navigate(['/profile']);
        }
        this.checkingMe = false;
        this.loading = false;
      },
      error: (err) => {
        this.checkingMe = false;
        this.loading = false;
        if (err.status === 404) {
          // Expected: user is authenticated but not yet onboarded → show form
          this.error = '';
        } else {
          this.error = 'Unable to check your profile. Please try again.';
        }
      }
    });
  }

  onSubmit() {
    this.submitting = true;
    this.error = '';

    this.citizenService.onboard(this.model).subscribe({
      next: (res) => {
        this.submitting = false;
        if (res.success) {
          this.authService.setCitizenId(res.data.id, res.data.fullName);
          this.router.navigate(['/profile']);
        }
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.message || 'Onboarding failed. Please try again.';
      }
    });
  }
}
