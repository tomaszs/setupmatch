import { Component, Inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Equipment } from '../../models/equipment.model';
import { EQUIPMENT_TYPE_LABELS } from '../../core/labels';

export interface RetireDialogData {
  equipment: Equipment;
}

export interface RetireDialogResult {
  reason: string;
}

@Component({
  selector: 'app-retire-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>Retire equipment</h2>
    <mat-dialog-content>
      <p class="equipment-summary">
        {{ typeLabel }} · {{ data.equipment.brand }} {{ data.equipment.model }}
      </p>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Reason</mat-label>
        <textarea matInput rows="3" [formControl]="reasonControl"></textarea>
        @if (reasonControl.hasError('required') && reasonControl.touched) {
          <mat-error>Reason is required</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close>Cancel</button>
      <button
        mat-stroked-button
        class="destructive-btn"
        [disabled]="reasonControl.invalid"
        (click)="submit()"
      >
        Retire
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .equipment-summary {
      color: var(--color-text-secondary);
      margin-bottom: 16px;
    }

    .full-width {
      width: 100%;
    }

    .destructive-btn {
      border-color: var(--color-error-text) !important;
      color: var(--color-error-text) !important;
    }
  `,
})
export class RetireDialogComponent {
  readonly reasonControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(512)],
  });

  constructor(
    private readonly dialogRef: MatDialogRef<RetireDialogComponent, RetireDialogResult>,
    @Inject(MAT_DIALOG_DATA) readonly data: RetireDialogData,
  ) {}

  get typeLabel(): string {
    return EQUIPMENT_TYPE_LABELS[this.data.equipment.type];
  }

  submit(): void {
    if (this.reasonControl.invalid) {
      this.reasonControl.markAsTouched();
      return;
    }

    this.dialogRef.close({ reason: this.reasonControl.value });
  }
}
