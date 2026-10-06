import { expect, Locator, test } from '@playwright/test';

async function selectSlotType(slot: Locator, typeLabel: string) {
  await slot.getByRole('button', { name: typeLabel, exact: true }).click();
}

test.describe('Scenario C allocation flow', () => {
  test('creates allocation, confirms, and assigns equipment', async ({ page }) => {
    const employeeId = `emp-e2e-${Date.now()}`;

    await page.goto('/allocations/new');
    await page.getByLabel('Employee ID').fill(employeeId);
    await selectSlotType(page.locator('.slot-card').first(), 'Keyboard');
    await page.getByRole('button', { name: 'Submit request' }).click();

    await expect(page.getByRole('heading', { name: 'Allocation detail' })).toBeVisible();
    await expect(page.locator('mat-chip.allocation-allocated')).toBeVisible();

    await page.getByRole('link', { name: 'Inventory' }).click();
    await expect(page.getByText('Reserved').first()).toBeVisible();

    await page.getByRole('link', { name: 'Allocations' }).click();
    await page.locator('tr.clickable-row', { hasText: employeeId }).click();

    await page.getByRole('button', { name: 'Confirm' }).click();
    await expect(page.locator('mat-chip.allocation-confirmed')).toBeVisible();

    await page.getByRole('link', { name: 'Inventory' }).click();
    await expect(page.getByText('Assigned').first()).toBeVisible();
  });
});

test.describe('Scenario D failed allocation', () => {
  test('shows failure reason and leaves inventory available', async ({ page }) => {
    await page.goto('/allocations/new');
    await page.getByLabel('Employee ID').fill(`emp-fail-${Date.now()}`);

    const slots = page.locator('.slot-card');
    await slots.nth(0).getByLabel('Score').fill('0.95');
    await page.getByRole('button', { name: 'Add slot' }).click();
    await slots.nth(1).getByLabel('Score').fill('0.95');

    await page.getByRole('button', { name: 'Submit request' }).click();

    await expect(page.locator('mat-chip.allocation-failed')).toBeVisible();
    await expect(page.getByText('Allocation failed')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Confirm' })).toHaveCount(0);
    await expect(page.locator('h2', { hasText: 'Allocated items' })).toHaveCount(0);
  });
});

test.describe('Scenario E cancel allocation', () => {
  test('cancels allocated request and releases equipment', async ({ page }) => {
    const employeeId = `emp-cancel-${Date.now()}`;

    await page.goto('/allocations/new');
    await page.getByLabel('Employee ID').fill(employeeId);
    await selectSlotType(page.locator('.slot-card').first(), 'Keyboard');
    await page.getByRole('button', { name: 'Submit request' }).click();

    await expect(page.locator('mat-chip.allocation-allocated')).toBeVisible();
    await page.getByRole('button', { name: 'Cancel' }).click();
    await expect(page.locator('mat-chip.allocation-cancelled')).toBeVisible();

    await page.getByRole('link', { name: 'Inventory' }).click();
    await expect(page.getByText('Available').first()).toBeVisible();
  });
});

test.describe('Brand suggestions', () => {
  test('shows brand pills for the selected equipment type', async ({ page }) => {
    const slot = page.locator('.slot-card').first();

    await page.goto('/allocations/new');
    await selectSlotType(slot, 'Main computer');
    await expect(slot.getByRole('button', { name: 'Apple' })).toBeVisible();
  });
});
