import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GrievanceService } from '../../services/grievance.service';
import { AuthService } from '../../services/auth.service';
import {
  GrievanceSubmitRequest,
  GrievanceResponse,
  GrievanceCategory,
  Priority,
  GrievanceStatus,
  GrievanceSearchParams,
  PageResponse,
} from '../../models/models';

@Component({
  selector: 'app-grievances',
  imports: [CommonModule, FormsModule],
  templateUrl: './grievances.component.html',
  styleUrl: './grievances.component.css'
})
export class GrievancesComponent implements OnInit {

  // ── Auth ─────────────────────────────────────────────────────────────────────
  citizenId = '';

  // ── Paginated list state ──────────────────────────────────────────────────────
  grievancePage: PageResponse<GrievanceResponse> | null = null;
  grievances: GrievanceResponse[] = [];
  listLoading = false;
  listError = '';

  // ── Filters ───────────────────────────────────────────────────────────────────
  filterSearch = '';
  filterStatus: GrievanceStatus | '' = '';
  filterPriority: Priority | '' = '';
  currentPage = 0;
  readonly pageSize = 10;

  // ── Submit form state ─────────────────────────────────────────────────────────
  title = '';
  description = '';
  category: GrievanceCategory = 'OTHER';
  priority: Priority = 'LOW';
  submitLoading = false;
  submitError = '';
  submitSuccess = '';

  // ── Dropdown options ──────────────────────────────────────────────────────────
  readonly categories: GrievanceCategory[] = [
    'SERVICE_DELIVERY', 'DOCUMENT_ISSUES', 'INCORRECT_CERTIFICATE',
    'BILLING', 'TECHNICAL', 'OTHER'
  ];
  readonly priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly statuses: GrievanceStatus[] = [
    'SUBMITTED', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'
  ];

  constructor(private grievanceService: GrievanceService, private auth: AuthService) {}

  ngOnInit(): void {
    this.citizenId = this.auth.citizenId;
    this.loadGrievances();
  }

  // ── List / search ─────────────────────────────────────────────────────────────

  loadGrievances(): void {
    this.listLoading = true;
    this.listError = '';

    const params: GrievanceSearchParams = {
      page: this.currentPage,
      size: this.pageSize,
      sortBy: 'createdAt',
      sortDir: 'desc',
      ...(this.filterSearch   ? { search:   this.filterSearch }            : {}),
      ...(this.filterStatus   ? { status:   this.filterStatus }            : {}),
      ...(this.filterPriority ? { priority: this.filterPriority }          : {}),
    };

    // Backend auto-sets citizenId = JWT subject for CITIZEN role.
    // No need to pass citizenId from the frontend.
    this.grievanceService.searchGrievances(params).subscribe({
      next: (res) => {
        this.listLoading = false;
        if (res.success) {
          this.grievancePage = res.data;
          this.grievances = res.data.content;
        }
      },
      error: (err) => {
        this.listLoading = false;
        this.listError = err?.error?.message || 'Failed to load grievances';
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadGrievances();
  }

  clearFilters(): void {
    this.filterSearch = '';
    this.filterStatus = '';
    this.filterPriority = '';
    this.currentPage = 0;
    this.loadGrievances();
  }

  goToPage(page: number): void {
    if (page < 0 || (this.grievancePage && page >= this.grievancePage.totalPages)) return;
    this.currentPage = page;
    this.loadGrievances();
  }

  get pageNumbers(): number[] {
    if (!this.grievancePage) return [];
    return Array.from({ length: this.grievancePage.totalPages }, (_, i) => i);
  }

  // ── Submit ────────────────────────────────────────────────────────────────────

  submit(): void {
    this.submitError = '';
    this.submitSuccess = '';

    if (!this.citizenId)                        { this.submitError = 'Not signed in'; return; }
    if (!this.title || this.title.length < 5)   { this.submitError = 'Title must be at least 5 characters'; return; }
    if (!this.description || this.description.length < 20) {
      this.submitError = 'Description must be at least 20 characters'; return;
    }

    this.submitLoading = true;

    const req: GrievanceSubmitRequest = {
      title: this.title,
      description: this.description,
      category: this.category,
      priority: this.priority,
    };

    this.grievanceService.submitGrievance(req).subscribe({
      next: (res) => {
        this.submitLoading = false;
        if (res.success) {
          this.submitSuccess = 'Grievance submitted successfully';
          this.title = '';
          this.description = '';
          this.currentPage = 0;
          this.loadGrievances();
        }
      },
      error: (err) => {
        this.submitLoading = false;
        this.submitError = err?.error?.message || 'Failed to submit grievance';
      }
    });
  }
}
