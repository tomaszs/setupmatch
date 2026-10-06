import { buildBrandOptionsByType, filterBrandSuggestions } from './brand-options';
import { Equipment } from '../models/equipment.model';

const sampleEquipment: Equipment[] = [
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
    brand: 'LG',
    model: '27UP850',
    state: 'available',
    condition_score: 0.85,
    purchase_date: '2023-01-01',
    retire_reason: null,
    retired_at: null,
  },
  {
    id: '3',
    type: 'keyboard',
    brand: 'Apple',
    model: 'Magic Keyboard',
    state: 'retired',
    condition_score: 0.9,
    purchase_date: '2023-01-01',
    retire_reason: 'old',
    retired_at: '2024-01-01T00:00:00Z',
  },
];

describe('brand-options', () => {
  it('builds sorted brand lists per equipment type excluding retired', () => {
    const options = buildBrandOptionsByType(sampleEquipment);

    expect(options.monitor).toEqual(['Dell', 'LG']);
    expect(options.keyboard).toBeUndefined();
  });

  it('filters brand suggestions by query', () => {
    const options = ['Apple', 'Dell', 'Logitech'];

    expect(filterBrandSuggestions('de', options)).toEqual(['Dell']);
    expect(filterBrandSuggestions('', options)).toEqual(options);
  });
});
