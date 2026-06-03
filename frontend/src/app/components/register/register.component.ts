import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { CitizenRegistrationRequest } from '../../models/models';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  model: CitizenRegistrationRequest = {
    fullName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    aadhaarNumber: '',
    address: '',
    state: '',
    pincode: ''
  };
  loading = false;
  error = '';
  success = '';
  registeredId = '';

  constructor(
    private citizenService: CitizenService,
    private router: Router
  ) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.success = '';
    this.citizenService.register(this.model).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.registeredId = res.data.id;
          this.success = `Registration successful! Your Citizen ID: ${res.data.id}`;
          localStorage.setItem('citizenId', res.data.id);
          localStorage.setItem('citizenName', res.data.fullName);
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
