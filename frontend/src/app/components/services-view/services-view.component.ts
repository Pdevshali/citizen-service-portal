import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CitizenService } from '../../services/citizen.service';
import { ServiceRequestResponse } from '../../models/models';

@Component({
  selector: 'app-services-view',
  templateUrl: './services-view.component.html',
  styleUrl: './services-view.component.css'
})
export class ServicesViewComponent {
  services: ServiceRequestResponse[] = [];
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private citizenService: CitizenService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.citizenService.getServices(id).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.services = res.data;
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to load services.';
      }
    });
  }
}
