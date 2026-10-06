import { Equipment, EquipmentType } from './equipment.model';

export type AllocationState = 'allocated' | 'failed' | 'confirmed' | 'cancelled';

export interface PolicySlot {
  type: EquipmentType;
  min_condition?: number;
  preferred_brand?: string;
}

export interface AllocationSummary {
  id: string;
  employee_id: string;
  state: AllocationState;
  item_count: number;
  created_at: string;
}

export interface AllocatedEquipment {
  policy_slot_index: number;
  equipment: Equipment;
}

export interface AllocationDetail {
  id: string;
  employee_id: string;
  state: AllocationState;
  failure_reason: string | null;
  policy: PolicySlot[];
  allocated_equipments: AllocatedEquipment[];
  created_at: string;
}

export interface CreateAllocationRequest {
  employee_id: string;
  policy: PolicySlot[];
}

export interface AllocationFilters {
  state?: AllocationState;
  employee_id?: string;
}
