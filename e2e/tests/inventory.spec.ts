import { expect, test } from '@playwright/test';

test.describe('Scenario A inventory', () => {
  test('loads seeded equipment inventory', async ({ page }) => {
    await page.goto('/inventory');

    await expect(page.getByRole('heading', { name: 'Equipment inventory' })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'MacBook Pro 14' })).toBeVisible();
    await expect(page.getByText('Available').first()).toBeVisible();
  });
});
