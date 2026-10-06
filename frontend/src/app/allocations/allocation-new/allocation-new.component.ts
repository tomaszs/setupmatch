import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { AllocationApiService } from '../../services/allocation-api.service';
import { EquipmentApiService } from '../../services/equipment-api.service';
import { EquipmentRefreshService } from '../../services/equipment-refresh.service';
import { PolicySlot } from '../../models/allocation.model';
import { EquipmentType } from '../../models/equipment.model';
import { ApiError } from '../../models/api-error.model';
import { EQUIPMENT_TYPE_LABELS, EQUIPMENT_TYPES } from '../../core/labels';
import { buildBrandOptionsByType } from '../../core/brand-options';
import { DEMO_EMPLOYEE_IDS, filterEmployeeSuggestions } from '../../core/demo-employees';
import {
  fadeCardAnimation,
  fadeInAnimation,
  slotItemAnimation,
  slotListAnimation,
} from '../../animations/fade.animations';
import { ConditionScoreFieldComponent } from '../../shared/condition-score-field/condition-score-field.component';

@Component({
  selector: 'app-allocation-new',
  standalone: true,
  animations: [fadeInAnimation, fadeCardAnimation, slotListAnimation, slotItemAnimation],
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
    ConditionScoreFieldComponent,
  ],
  templateUrl: './allocation-new.component.html',
  styleUrl: './allocation-new.component.scss',
})
export class AllocationNewComponent implements OnInit {
  private readonly allocationApi = inject(AllocationApiService);
  private readonly equipmentApi = inject(EquipmentApiService);
  private readonly refreshService = inject(EquipmentRefreshService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly equipmentTypes = EQUIPMENT_TYPES;
  readonly typeLabels = EQUIPMENT_TYPE_LABELS;
  readonly demoEmployeeIds = DEMO_EMPLOYEE_IDS;

  submitting = false;
  serverFieldErrors: Record<string, string> = {};
  brandOptionsByType: Partial<Record<EquipmentType, string[]>> = {};

  form = new FormGroup({
    employee_id: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(128)],
    }),
    policy: new FormArray<FormGroup<{
      type: FormControl<EquipmentType>;
      min_condition: FormControl<number | null>;
      preferred_brand: FormControl<string>;
    }>>([]),
  });

  constructor() {
    this.addSlot();
  }

  ngOnInit(): void {
    this.equipmentApi
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (equipment) => {
          this.brandOptionsByType = buildBrandOptionsByType(equipment);
        },
      });
  }

  get policySlots(): FormArray {
    return this.form.controls.policy;
  }

  addSlot(): void {
    this.policySlots.push(
      new FormGroup({
        type: new FormControl<EquipmentType>('monitor', {
          nonNullable: true,
          validators: [Validators.required],
        }),
        min_condition: new FormControl<number | null>(null, {
          validators: [Validators.min(0), Validators.max(1)],
        }),
        preferred_brand: new FormControl('', {
          nonNullable: true,
          validators: [Validators.maxLength(128)],
        }),
      }),
    );
  }

  removeSlot(index: number): void {
    if (this.policySlots.length <= 1) {
      return;
    }

    this.policySlots.removeAt(index);
  }

  employeeSuggestions(): string[] {
    const query = this.form.controls.employee_id.value;
    return filterEmployeeSuggestions(query, this.demoEmployeeIds);
  }

  brandPills(slotIndex: number): string[] {
    const type = this.slotGroup(slotIndex).controls.type.value;
    return this.brandOptionsByType[type] ?? [];
  }

  setSlotType(slotIndex: number, type: EquipmentType): void {
    this.slotGroup(slotIndex).controls.type.setValue(type);
  }

  setPreferredBrand(slotIndex: number, brand: string): void {
    this.slotGroup(slotIndex).controls.preferred_brand.setValue(brand);
  }

  slotGroup(index: number): FormGroup<{
    type: FormControl<EquipmentType>;
    min_condition: FormControl<number | null>;
    preferred_brand: FormControl<string>;
  }> {
    return this.policySlots.at(index) as FormGroup<{
      type: FormControl<EquipmentType>;
      min_condition: FormControl<number | null>;
      preferred_brand: FormControl<string>;
    }>;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.serverFieldErrors = {};

    const policy: PolicySlot[] = this.policySlots.controls.map((slot) => {
      const value = slot.getRawValue();
      const entry: PolicySlot = { type: value.type };

      if (value.min_condition !== null && value.min_condition !== undefined) {
        entry.min_condition = value.min_condition;
      }

      if (value.preferred_brand.trim()) {
        entry.preferred_brand = value.preferred_brand.trim();
      }

      return entry;
    });

    this.allocationApi
      .create({
        employee_id: this.form.controls.employee_id.value,
        policy,
      })
      .subscribe({
        next: (detail) => {
          this.submitting = false;
          this.refreshService.notifyRefresh();
          this.router.navigate(['/allocations', detail.id]);
        },
        error: (err: HttpErrorResponse) => {
          this.submitting = false;
          const apiError = err.error as ApiError | undefined;
          this.serverFieldErrors = apiError?.field_errors ?? {};
          this.snackBar.open(apiError?.message ?? 'Unable to create allocation', 'Close', {
            duration: 5000,
          });
        },
      });
  }

  fieldError(field: string): string | null {
    return this.serverFieldErrors[field] ?? null;
  }
}
