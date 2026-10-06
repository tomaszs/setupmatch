import { DecimalPipe } from '@angular/common';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { EquipmentApiService } from '../services/equipment-api.service';
import { EquipmentRefreshService } from '../services/equipment-refresh.service';
import { Equipment, EquipmentState, EquipmentType } from '../models/equipment.model';
import { ApiError } from '../models/api-error.model';
import {
  EQUIPMENT_STATE_LABELS,
  EQUIPMENT_STATES,
  EQUIPMENT_TYPE_LABELS,
  EQUIPMENT_TYPES,
} from '../core/labels';
import { StateChipComponent } from '../shared/state-chip/state-chip.component';
import { ErrorBannerComponent } from '../shared/error-banner/error-banner.component';
import {
  RetireDialogComponent,
  RetireDialogResult,
} from '../shared/retire-dialog/retire-dialog.component';
import {
  RegisterEquipmentDialogComponent,
} from '../shared/register-equipment-dialog/register-equipment-dialog.component';
import { fadeCardAnimation, fadeInAnimation } from '../animations/fade.animations';

@Component({
  selector: 'app-inventory',
  standalone: true,
  animations: [fadeInAnimation, fadeCardAnimation],
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    StateChipComponent,
    ErrorBannerComponent,
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss',
})
export class InventoryComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly equipmentApi = inject(EquipmentApiService);
  private readonly refreshService = inject(EquipmentRefreshService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly displayedColumns = ['type', 'brand', 'model', 'condition', 'state', 'actions'];
  readonly equipmentTypes = EQUIPMENT_TYPES;
  readonly equipmentStates = EQUIPMENT_STATES;
  readonly typeLabels = EQUIPMENT_TYPE_LABELS;
  readonly stateLabels = EQUIPMENT_STATE_LABELS;

  equipments: Equipment[] = [];
  loading = false;
  retiringId: string | null = null;
  errorMessage: string | null = null;

  stateFilter = new FormControl<EquipmentState | ''>('', { nonNullable: true });
  typeFilter = new FormControl<EquipmentType | ''>('', { nonNullable: true });
  showRetired = new FormControl(false, { nonNullable: true });

  ngOnInit(): void {
    this.loadEquipments();

    this.stateFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadEquipments());

    this.typeFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadEquipments());

    this.showRetired.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadEquipments());

    this.refreshService.refresh$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadEquipments());
  }

  loadEquipments(): void {
    this.loading = true;
    this.errorMessage = null;

    this.equipmentApi
      .list({
        state: this.stateFilter.value || undefined,
        type: this.typeFilter.value || undefined,
        include_retired: this.showRetired.value,
      })
      .subscribe({
        next: (items) => {
          this.equipments = items;
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.errorMessage = this.extractMessage(err);
        },
      });
  }

  openRegisterDialog(): void {
    const dialogRef = this.dialog.open(RegisterEquipmentDialogComponent, {
      width: '560px',
      maxWidth: '95vw',
      data: { equipments: this.equipments },
    });

    dialogRef.afterClosed().subscribe((registered) => {
      if (!registered) {
        return;
      }

      this.snackBar.open('Equipment registered', 'Close', {
        duration: 3000,
        panelClass: 'success-snackbar',
      });
      this.loadEquipments();
      this.refreshService.notifyRefresh();
    });
  }

  openRetireDialog(equipment: Equipment): void {
    const dialogRef = this.dialog.open<
      RetireDialogComponent,
      { equipment: Equipment },
      RetireDialogResult
    >(RetireDialogComponent, {
      width: '480px',
      data: { equipment },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }

      this.retiringId = equipment.id;

      this.equipmentApi.retire(equipment.id, { reason: result.reason }).subscribe({
        next: () => {
          this.retiringId = null;
          this.snackBar.open('Equipment retired', 'Close', {
            duration: 3000,
            panelClass: 'success-snackbar',
          });
          this.loadEquipments();
        },
        error: (err: HttpErrorResponse) => {
          this.retiringId = null;
          this.snackBar.open(this.extractMessage(err), 'Close', { duration: 5000 });
        },
      });
    });
  }

  conditionPercent(score: number): number {
    return Math.round(score * 100);
  }

  isRetiredRow(equipment: Equipment): boolean {
    return equipment.state === 'retired';
  }

  typeLabel(type: EquipmentType): string {
    return this.typeLabels[type];
  }

  private extractMessage(err: HttpErrorResponse): string {
    const apiError = err.error as ApiError | undefined;
    return apiError?.message ?? 'Unable to load equipment. Please try again.';
  }
}
