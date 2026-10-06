export type EquipmentType = 'main_computer' | 'monitor' | 'keyboard' | 'mouse';
export type EquipmentState = 'available' | 'reserved' | 'assigned' | 'retired';

export interface Equipment {
  id: string;
  type: EquipmentType;
  brand: string;
  model: string;
  state: EquipmentState;
  condition_score: number;
  purchase_date: string;
  retire_reason: string | null;
  retired_at: string | null;
}

export interface CreateEquipmentRequest {
  type: EquipmentType;
  brand: string;
  model: string;
  condition_score: number;
  purchase_date: string;
}

export interface RetireEquipmentRequest {
  reason: string;
}

export interface EquipmentFilters {
  state?: EquipmentState;
  type?: EquipmentType;
  include_retired?: boolean;
}
