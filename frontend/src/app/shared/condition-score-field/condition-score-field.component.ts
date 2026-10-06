import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  clampCondition,
  CONDITION_PRESETS,
  formatConditionPreset,
  isConditionPresetActive,
  roundCondition,
} from '../../core/condition-score';

@Component({
  selector: 'app-condition-score-field',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule],
  templateUrl: './condition-score-field.component.html',
  styleUrl: './condition-score-field.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ConditionScoreFieldComponent),
      multi: true,
    },
  ],
})
export class ConditionScoreFieldComponent implements ControlValueAccessor {
  @Input() label = 'Score';

  readonly conditionPresets = CONDITION_PRESETS;

  value: number | null = null;
  disabled = false;

  private onChange: (value: number | null) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  writeValue(value: number | null): void {
    this.value = value;
  }

  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInputChange(rawValue: string): void {
    if (rawValue === '') {
      this.updateValue(null);
      return;
    }

    const parsed = Number(rawValue);

    if (Number.isNaN(parsed)) {
      return;
    }

    this.updateValue(clampCondition(parsed));
  }

  setPreset(preset: number): void {
    this.updateValue(preset);
  }

  adjust(delta: number): void {
    const current = this.value ?? 0;
    this.updateValue(clampCondition(roundCondition(current + delta)));
  }

  isPresetActive(preset: number): boolean {
    return isConditionPresetActive(this.value, preset);
  }

  formatPreset(value: number): string {
    return formatConditionPreset(value);
  }

  markTouched(): void {
    this.onTouched();
  }

  private updateValue(value: number | null): void {
    this.value = value;
    this.onChange(value);
    this.onTouched();
  }
}
