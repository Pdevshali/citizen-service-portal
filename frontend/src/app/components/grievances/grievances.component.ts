import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GrievanceService } from '../../services/grievance.service';
import { AuthService } from '../../services/auth.service';
import { GrievanceSubmitRequest, GrievanceResponse, GrievanceCategory, Priority } from '../../models/models';

@Component({
  selector: 'app-grievances',
  imports: [CommonModule, FormsModule],
  templateUrl: './grievances.component.html',
  styleUrl: './grievances.component.css'
})
export class GrievancesComponent implements OnInit {
  citizenId = '';
  grievances: GrievanceResponse[] = [];

  // Form fields
  title = '';
  description = '';
  category: GrievanceCategory = 'OTHER';
  priority: Priority = 'LOW';
  loading = false;
  error = '';
  success = '';

  categories = ['SERVICE_DELIVERY','DOCUMENT_ISSUES','INCORRECT_CERTIFICATE','BILLING','TECHNICAL','OTHER'];
  priorities = ['LOW','MEDIUM','HIGH','CRITICAL'];

  constructor(private grievanceService: GrievanceService, private auth: AuthService) {}

  ngOnInit(): void {
    this.citizenId = this.auth.citizenId;
    // Always attempt to load grievances for the authenticated user via /me endpoint
    this.loadGrievances();
  }

  loadGrievances() {
    this.grievanceService.getMyGrievances().subscribe({
      next: (res) => { if (res.success) this.grievances = res.data; },
      error: (err) => { this.error = err?.error?.message || 'Failed to load grievances'; }
    });
  }

  submit() {
    if (!this.citizenId) { this.error = 'Not signed in'; return; }
    if (!this.title || this.title.length < 5) { this.error = 'Title must be at least 5 characters'; return; }
    if (!this.description || this.description.length < 20) { this.error = 'Description must be at least 20 characters'; return; }

    this.loading = true; this.error = ''; this.success = '';
    // Do NOT send citizenId — backend will derive it from the authenticated principal
    const req: GrievanceSubmitRequest = {
      title: this.title,
      description: this.description,
      category: this.category as GrievanceCategory,
      priority: this.priority as Priority,
      attachmentUrl: undefined
    } as GrievanceSubmitRequest;

    this.grievanceService.submitGrievance(req).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.success = 'Grievance submitted successfully';
          this.title = '';
          this.description = '';
          this.loadGrievances();
        }
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Failed to submit grievance'; }
    });
  }
}
