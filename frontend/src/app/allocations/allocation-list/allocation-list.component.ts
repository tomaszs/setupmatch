import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { HttpErrorResponse } from '@angular/common/http';
import { AllocationApiService } from '../../services/allocation-api.service';
import { AllocationSummary } from '../../models/allocation.model';
import { ApiError } from '../../models/api-error.model';
import { StateChipComponent } from '../../shared/state-chip/state-chip.component';
import { ErrorBannerComponent } from '../../shared/error-banner/error-banner.component';
import { fadeCardAnimation, fadeInAnimation } from '../../animations/fade.animations';

@Component({
  selector: 'app-allocation-list',
  standalone: true,
  animations: [fadeInAnimation, fadeCardAnimation],
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatProgressBarModule,
    MatTableModule,
    StateChipComponent,
    ErrorBannerComponent,
  ],
  templateUrl: './allocation-list.component.html',
  styleUrl: './allocation-list.component.scss',
})
export class AllocationListComponent implements OnInit {
  private readonly allocationApi = inject(AllocationApiService);
  private readonly router = inject(Router);

  readonly displayedColumns = ['employee_id', 'state', 'item_count', 'created_at'];

  allocations: AllocationSummary[] = [];
  loading = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadAllocations();
  }

  loadAllocations(): void {
    this.loading = true;
    this.errorMessage = null;

    this.allocationApi.list().subscribe({
      next: (items) => {
        this.allocations = items;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const apiError = err.error as ApiError | undefined;
        this.errorMessage = apiError?.message ?? 'Unable to load allocations. Please try again.';
      },
    });
  }

  openDetail(allocation: AllocationSummary): void {
    this.router.navigate(['/allocations', allocation.id]);
  }
}
