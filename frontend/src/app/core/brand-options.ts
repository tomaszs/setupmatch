import { Equipment, EquipmentType } from '../models/equipment.model';

export function buildBrandOptionsByType(equipment: Equipment[]): Partial<Record<EquipmentType, string[]>> {
  const brandsByType = new Map<EquipmentType, Set<string>>();

  for (const item of equipment) {
    if (item.state === 'retired') {
      continue;
    }

    const brands = brandsByType.get(item.type) ?? new Set<string>();
    brands.add(item.brand);
    brandsByType.set(item.type, brands);
  }

  const result: Partial<Record<EquipmentType, string[]>> = {};

  for (const [type, brands] of brandsByType.entries()) {
    result[type] = [...brands].sort((a, b) => a.localeCompare(b));
  }

  return result;
}

export function filterBrandSuggestions(query: string, options: string[]): string[] {
  const normalized = query.trim().toLowerCase();

  if (!normalized) {
    return options;
  }

  return options.filter((brand) => brand.toLowerCase().includes(normalized));
}
