import { distinctBrandsForType, distinctModelsForTypeAndBrand } from './equipment-options';
import { Equipment } from '../models/equipment.model';

const equipmentFixture: Equipment[] = [
  {
    id: '1',
    type: 'monitor',
    brand: 'Dell',
    model: 'U2723QE',
    state: 'available',
    condition_score: 0.9,
    purchase_date: '2024-01-01',
    retire_reason: null,
    retired_at: null,
  },
  {
    id: '2',
    type: 'monitor',
    brand: 'Dell',
    model: 'U2724D',
    state: 'available',
    condition_score: 0.85,
    purchase_date: '2024-02-01',
    retire_reason: null,
    retired_at: null,
  },
];

describe('equipment-options', () => {
  it('returns distinct brands and models from inventory', () => {
    expect(distinctBrandsForType(equipmentFixture, 'monitor')).toEqual(['Dell']);
    expect(distinctModelsForTypeAndBrand(equipmentFixture, 'monitor', 'Dell')).toEqual([
      'U2723QE',
      'U2724D',
    ]);
  });
});
