import { test, expect } from '@playwright/test';

test.describe('Administration', () => {

    test('Historientypen-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/historientypen');

        // Vorhandene Historientypen aus Testdaten
        await expect(page.locator('[data-testid^="histtype-row-"]').first()).toBeVisible();
    });

    test('Neuen Historientyp anlegen', async ({ page }) => {
        await page.goto('/admin/historientypen');

        await page.locator('button:has-text("Neuer Historientyp")').click();
        await page.locator('#new-ht-description').fill('E2E-Testtyp');
        await page.locator('button[type="submit"][form="new-ht-form"]').click();

        // Nach Anlage: neuer Typ in der Tabelle
        await expect(page.locator('td:has-text("E2E-Testtyp")')).toBeVisible();
    });

    test('Positionsstatus-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await expect(page.locator('[data-testid^="posstatus-row-"]').first()).toBeVisible();
    });

    test('Neuen Positionsstatus anlegen', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await page.locator('button:has-text("Neuer Status")').click();
        await page.locator('#new-status-description').fill('E2E-Status');
        await page.locator('#new-status-color').fill('#e0f2fe');
        await page.locator('#new-status-colortext').fill('#0369a1');
        await page.locator('button[type="submit"][form="new-status-form"]').click();

        await expect(page.locator('td:has-text("E2E-Status")')).toBeVisible();
    });

    test('Tags-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/tags');

        await expect(page.locator('[data-testid^="tag-row-"]').first()).toBeVisible();
    });

    test('Neuen Tag anlegen', async ({ page }) => {
        await page.goto('/admin/tags');

        await page.locator('#new-tag-type').selectOption('SCHWERPUNKT');
        await page.locator('#new-tag-name').fill('E2E-Tag-Playwright');
        await page.locator('button[type="submit"]:has-text("Tag anlegen")').click();

        await expect(page.locator('td:has-text("E2E-Tag-Playwright")')).toBeVisible();
    });

});
