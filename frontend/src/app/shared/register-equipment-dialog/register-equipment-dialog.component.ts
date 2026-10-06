import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { EquipmentApiService } from '../../services/equipment-api.service';
import { CreateEquipmentRequest, Equipment, EquipmentType } from '../../models/equipment.model';
import { ApiError } from '../../models/api-error.model';
import { EQUIPMENT_TYPE_LABELS, EQUIPMENT_TYPES } from '../../core/labels';
import { distinctBrandsForType, distinctModelsForTypeAndBrand } from '../../core/equipment-options';
import { ConditionScoreFieldComponent } from '../condition-score-field/condition-score-field.component';

export interface RegisterEquipmentDialogData {
  equipments: Equipment[];
}

@Component({
  selector: 'app-register-equipment-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    ConditionScoreFieldComponent,
  ],
  templateUrl: './register-equipment-dialog.component.html',
  styleUrl: './register-equipment-dialog.component.scss',
})
export class RegisterEquipmentDialogComponent {
  readonly equipmentTypes = EQUIPMENT_TYPES;
  readonly typeLabels = EQUIPMENT_TYPE_LABELS;

  submitting = false;
  serverFieldErrors: Record<string, string> = {};

  form = new FormGroup({
    type: new FormControl<EquipmentType>('main_computer', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    brand: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(128)],
    }),
    model: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(128)],
    }),
    condition_score: new FormControl(0.85, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0), Validators.max(1)],
    }),
    purchase_date: new FormControl<Date | null>(null, {
      validators: [Validators.required],
    }),
  });

  constructor(
    private readonly dialogRef: MatDialogRef<RegisterEquipmentDialogComponent, boolean>,
    private readonly equipmentApi: EquipmentApiService,
    @Inject(MAT_DIALOG_DATA) private readonly data: RegisterEquipmentDialogData,
  ) {}

  brandPills(): string[] {
    return distinctBrandsForType(this.data.equipments, this.form.controls.type.value);
  }

  modelPills(): string[] {
    return distinctModelsForTypeAndBrand(
      this.data.equipments,
      this.form.controls.type.value,
      this.form.controls.brand.value,
    );
  }

  setType(type: EquipmentType): void {
    this.form.controls.type.setValue(type);
  }

  setBrand(brand: string): void {
    this.form.controls.brand.setValue(brand);
  }

  setModel(model: string): void {
    this.form.controls.model.setValue(model);
  }

  typeLabel(type: EquipmentType): string {
    return this.typeLabels[type];
  }

  fieldError(field: string): string | null {
    return this.serverFieldErrors[field] ?? null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.serverFieldErrors = {};

    const raw = this.form.getRawValue();
    const payload: CreateEquipmentRequest = {
      type: raw.type,
      brand: raw.brand,
      model: raw.model,
      condition_score: raw.condition_score,
      purchase_date: this.formatPurchaseDate(raw.purchase_date),
    };

    this.equipmentApi.create(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting = false;
        const apiError = err.error as ApiError | undefined;
        this.serverFieldErrors = apiError?.field_errors ?? {};
      },
    });
  }

  private formatPurchaseDate(date: Date | null): string {
    if (!date) {
      return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
