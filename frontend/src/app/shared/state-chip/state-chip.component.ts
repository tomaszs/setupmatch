import { Component, Input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { AllocationState } from '../../models/allocation.model';
import { EquipmentState } from '../../models/equipment.model';
import {
  ALLOCATION_STATE_LABELS,
  EQUIPMENT_STATE_LABELS,
} from '../../core/labels';

@Component({
  selector: 'app-state-chip',
  standalone: true,
  imports: [MatChipsModule],
  template: `
    <mat-chip [class]="chipClass">{{ label }}</mat-chip>
  `,
  styles: `
    :host {
      display: inline-block;
    }

    mat-chip {
      font-family: var(--font-mono);
      font-size: 12px;
      font-weight: 500;
      border: none;
      border-radius: var(--radius-pill);
      pointer-events: none;
      cursor: default;
      --mdc-chip-hover-state-layer-opacity: 0;
      --mdc-chip-focus-state-layer-opacity: 0;
      --mat-chip-hover-state-layer-opacity: 0;
      --mat-chip-focus-state-layer-opacity: 0;
    }

    mat-chip::before {
      opacity: 0 !important;
    }

    .equipment-available {
      background: var(--color-success-bg) !important;
      color: var(--color-success-text) !important;
    }

    .equipment-reserved {
      background: var(--color-info-bg) !important;
      color: var(--color-info-text) !important;
    }

    .equipment-assigned {
      background: var(--color-surface-muted) !important;
      color: var(--color-text-secondary) !important;
    }

    .equipment-retired {
      background: var(--color-error-bg) !important;
      color: var(--color-error-text) !important;
    }

    .allocation-allocated {
      background: var(--color-info-bg) !important;
      color: var(--color-info-text) !important;
    }

    .allocation-failed {
      background: var(--color-error-bg) !important;
      color: var(--color-error-text) !important;
    }

    .allocation-confirmed {
      background: var(--color-success-bg) !important;
      color: var(--color-success-text) !important;
    }

    .allocation-cancelled {
      background: var(--color-surface-muted) !important;
      color: var(--color-text-muted) !important;
    }
  `,
})
export class StateChipComponent {
  @Input({ required: true }) kind!: 'equipment' | 'allocation';
  @Input({ required: true }) state!: EquipmentState | AllocationState;

  get label(): string {
    if (this.kind === 'equipment') {
      return EQUIPMENT_STATE_LABELS[this.state as EquipmentState];
    }

    return ALLOCATION_STATE_LABELS[this.state as AllocationState];
  }

  get chipClass(): string {
    return `${this.kind}-${this.state}`;
  }
}
