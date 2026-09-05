import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { RegisterComponent } from './components/register/register.component';
import { OnboardingComponent } from './components/onboarding/onboarding.component';
import { ProfileComponent } from './components/profile/profile.component';
import { KycComponent } from './components/kyc/kyc.component';
import { DocumentsComponent } from './components/documents/documents.component';
import { CertificatesComponent } from './components/certificates/certificates.component';
import { ServicesViewComponent } from './components/services-view/services-view.component';
import { GrievancesComponent } from './components/grievances/grievances.component';
import { AuthGuard } from './core/auth.guard';

export const routes: Routes = [
  // ── Public routes — no authentication required ─────────────────────────────
  { path: '', component: HomeComponent },

  // Legacy citizen-direct registration (kept for admin/testing, not shown in UI)
  { path: 'register', component: RegisterComponent },

  // ── Onboarding — protected, shown once after Keycloak login/registration ───
  // Keycloak redirects here after login/register (redirectUri = /onboarding).
  // The component checks /api/citizens/me:
  //   404 → show profile form
  //   200 → already onboarded → redirect to /profile
  {
    path: 'onboarding',
    component: OnboardingComponent,
    canActivate: [AuthGuard],
  },

  // ── Protected routes — require Keycloak authentication ────────────────────

  // /profile — no ID in URL; backend derives owner from JWT sub
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'kyc',
    component: KycComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'documents',
    component: DocumentsComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'certificates',
    component: CertificatesComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'grievances',
    component: GrievancesComponent,
    canActivate: [AuthGuard],
  },

  {
    path: 'services/:id',
    component: ServicesViewComponent,
    canActivate: [AuthGuard],
  },

  // ── Fallback ──────────────────────────────────────────────────────────────
  { path: '**', redirectTo: '' },
];
