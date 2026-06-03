import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { KycComponent } from './components/kyc/kyc.component';
import { DocumentsComponent } from './components/documents/documents.component';
import { CertificatesComponent } from './components/certificates/certificates.component';
import { ServicesViewComponent } from './components/services-view/services-view.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile/:id', component: ProfileComponent },
  { path: 'kyc', component: KycComponent },
  { path: 'documents', component: DocumentsComponent },
  { path: 'certificates', component: CertificatesComponent },
  { path: 'services/:id', component: ServicesViewComponent }
];
