export const CONDITION_PRESETS = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1];

export function clampCondition(value: number): number {
  return roundCondition(Math.min(1, Math.max(0, value)));
}

export function roundCondition(value: number): number {
  return Math.round(value * 100) / 100;
}

export function formatConditionPreset(value: number): string {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1);
}

export function isConditionPresetActive(value: number | null | undefined, preset: number): boolean {
  if (value === null || value === undefined) {
    return false;
  }

  return Math.abs(value - preset) < 0.001;
}
