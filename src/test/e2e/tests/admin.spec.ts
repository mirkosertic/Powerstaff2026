import { test, expect } from '@playwright/test';

test.describe('Administration', () => {

    test('Historientypen-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/historientypen');

        await expect(page.locator('[data-testid^="histtype-row-"]').first()).toBeVisible();
    });

    test('Neuen Historientyp anlegen', async ({ page }) => {
        await page.goto('/admin/historientypen');

        await page.locator('[data-testid="btn-new-histtype"]').click();
        await page.locator('#new-ht-description').fill('E2E-Testtyp');
        await page.locator('[data-testid="btn-submit-histtype"]').click();
        await page.waitForURL(/\/admin\/historientypen/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Testtyp$/ }).first()).toBeVisible();
    });

    test('Positionsstatus-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await expect(page.locator('[data-testid^="posstatus-row-"]').first()).toBeVisible();
    });

    test('Neuen Positionsstatus anlegen', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await page.locator('[data-testid="btn-new-posstatus"]').click();
        await page.locator('#new-status-description').fill('E2E-Status');
        await page.locator('#new-status-color').fill('#e0f2fe');
        await page.locator('#new-status-colortext').fill('#0369a1');
        await page.locator('[data-testid="btn-submit-posstatus"]').click();
        await page.waitForURL(/\/admin\/positionsstatus/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Status$/ }).first()).toBeVisible();
    });

    test('Tags-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/tags');

        await expect(page.locator('[data-testid^="tag-row-"]').first()).toBeVisible();
    });

    test('Neuen Tag anlegen', async ({ page }) => {
        await page.goto('/admin/tags');

        await page.locator('#new-tag-type').selectOption('SCHWERPUNKT');
        await page.locator('#new-tag-name').fill('E2E-Tag-Playwright');
        await page.locator('[data-testid="btn-submit-tag"]').click();
        await page.waitForURL(/\/admin\/tags/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Tag-Playwright$/ }).first()).toBeVisible();
    });

});
