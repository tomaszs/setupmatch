import { Equipment, EquipmentType } from '../models/equipment.model';

export function distinctBrandsForType(equipment: Equipment[], type: EquipmentType): string[] {
  const brands = new Set<string>();

  for (const item of equipment) {
    if (item.type === type && item.state !== 'retired') {
      brands.add(item.brand);
    }
  }

  return [...brands].sort((a, b) => a.localeCompare(b));
}

export function distinctModelsForTypeAndBrand(
  equipment: Equipment[],
  type: EquipmentType,
  brand: string,
): string[] {
  const normalizedBrand = brand.trim().toLowerCase();

  if (!normalizedBrand) {
    return [];
  }

  const models = new Set<string>();

  for (const item of equipment) {
    if (
      item.type === type &&
      item.state !== 'retired' &&
      item.brand.toLowerCase() === normalizedBrand
    ) {
      models.add(item.model);
    }
  }

  return [...models].sort((a, b) => a.localeCompare(b));
}
