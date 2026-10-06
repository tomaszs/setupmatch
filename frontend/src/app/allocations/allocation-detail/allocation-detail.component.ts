import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { HttpErrorResponse } from '@angular/common/http';
import { AllocationApiService } from '../../services/allocation-api.service';
import { EquipmentRefreshService } from '../../services/equipment-refresh.service';
import { AllocationDetail } from '../../models/allocation.model';
import { ApiError } from '../../models/api-error.model';
import { EQUIPMENT_TYPE_LABELS } from '../../core/labels';
import { StateChipComponent } from '../../shared/state-chip/state-chip.component';
import { ErrorBannerComponent } from '../../shared/error-banner/error-banner.component';
import { fadeCardAnimation, fadeInAnimation } from '../../animations/fade.animations';

@Component({
  selector: 'app-allocation-detail',
  standalone: true,
  animations: [fadeInAnimation, fadeCardAnimation],
  imports: [
    DatePipe,
    DecimalPipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule,
    StateChipComponent,
    ErrorBannerComponent,
  ],
  templateUrl: './allocation-detail.component.html',
  styleUrl: './allocation-detail.component.scss',
})
export class AllocationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly allocationApi = inject(AllocationApiService);
  private readonly refreshService = inject(EquipmentRefreshService);
  private readonly snackBar = inject(MatSnackBar);

  readonly typeLabels = EQUIPMENT_TYPE_LABELS;
  readonly displayedColumns = ['slot', 'type', 'brand', 'model', 'condition', 'state'];

  allocation: AllocationDetail | null = null;
  loading = false;
  actionInFlight = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.loadAllocation(id);
    }
  }

  loadAllocation(id: string): void {
    this.loading = true;
    this.errorMessage = null;

    this.allocationApi.get(id).subscribe({
      next: (detail) => {
        this.allocation = detail;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const apiError = err.error as ApiError | undefined;
        this.errorMessage = apiError?.message ?? 'Unable to load allocation. Please try again.';
      },
    });
  }

  confirm(): void {
    if (!this.allocation || this.actionInFlight) {
      return;
    }

    this.actionInFlight = true;

    this.allocationApi.confirm(this.allocation.id).subscribe({
      next: (detail) => {
        this.allocation = detail;
        this.actionInFlight = false;
        this.refreshService.notifyRefresh();
        this.snackBar.open('Allocation confirmed', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
      },
      error: (err: HttpErrorResponse) => {
        this.actionInFlight = false;
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'Unable to confirm allocation', 'Close', {
          duration: 5000,
        });
      },
    });
  }

  cancel(): void {
    if (!this.allocation || this.actionInFlight) {
      return;
    }

    this.actionInFlight = true;

    this.allocationApi.cancel(this.allocation.id).subscribe({
      next: (detail) => {
        this.allocation = detail;
        this.actionInFlight = false;
        this.refreshService.notifyRefresh();
        this.snackBar.open('Allocation cancelled', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
      },
      error: (err: HttpErrorResponse) => {
        this.actionInFlight = false;
        const apiError = err.error as ApiError | undefined;
        this.snackBar.open(apiError?.message ?? 'Unable to cancel allocation', 'Close', {
          duration: 5000,
        });
      },
    });
  }

  policySlotLabel(index: number): string {
    const slot = this.allocation?.policy[index];

    if (!slot) {
      return `Slot ${index + 1}`;
    }

    const parts = [this.typeLabels[slot.type]];

    if (slot.min_condition !== undefined && slot.min_condition !== null) {
      parts.push(`min ${slot.min_condition}`);
    }

    if (slot.preferred_brand) {
      parts.push(slot.preferred_brand);
    }

    return parts.join(' · ');
  }

  typeLabel(type: string): string {
    return this.typeLabels[type as keyof typeof this.typeLabels] ?? type;
  }

  retryLoad(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.loadAllocation(id);
    }
  }
}
