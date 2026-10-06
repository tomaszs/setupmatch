import { EquipmentState, EquipmentType } from '../models/equipment.model';
import { AllocationState } from '../models/allocation.model';

export const EQUIPMENT_TYPE_LABELS: Record<EquipmentType, string> = {
  main_computer: 'Main computer',
  monitor: 'Monitor',
  keyboard: 'Keyboard',
  mouse: 'Mouse',
};

export const EQUIPMENT_STATE_LABELS: Record<EquipmentState, string> = {
  available: 'Available',
  reserved: 'Reserved',
  assigned: 'Assigned',
  retired: 'Retired',
};

export const ALLOCATION_STATE_LABELS: Record<AllocationState, string> = {
  allocated: 'Allocated',
  failed: 'Failed',
  confirmed: 'Confirmed',
  cancelled: 'Cancelled',
};

export const EQUIPMENT_TYPES: EquipmentType[] = ['main_computer', 'monitor', 'keyboard', 'mouse'];
export const EQUIPMENT_STATES: EquipmentState[] = ['available', 'reserved', 'assigned', 'retired'];
