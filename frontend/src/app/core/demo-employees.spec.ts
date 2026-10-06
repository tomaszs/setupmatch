import { DEMO_EMPLOYEE_IDS, filterEmployeeSuggestions } from './demo-employees';

describe('demo-employees', () => {
  it('filters employee suggestions by query', () => {
    expect(filterEmployeeSuggestions('design', DEMO_EMPLOYEE_IDS)).toEqual(['emp-design-7']);
    expect(filterEmployeeSuggestions('', DEMO_EMPLOYEE_IDS)).toEqual(DEMO_EMPLOYEE_IDS);
  });
});
