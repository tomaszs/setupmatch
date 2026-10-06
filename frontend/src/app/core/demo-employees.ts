export const DEMO_EMPLOYEE_IDS = [
  'emp-001',
  'emp-002',
  'emp-042',
  'emp-qa-1',
  'emp-design-7',
  'emp-engineering-12',
  'emp-sales-3',
  'emp-support-9',
];

export function filterEmployeeSuggestions(query: string, options: string[] = DEMO_EMPLOYEE_IDS): string[] {
  const normalized = query.trim().toLowerCase();

  if (!normalized) {
    return options;
  }

  return options.filter((employeeId) => employeeId.toLowerCase().includes(normalized));
}
