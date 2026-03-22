import { test, expect } from '@playwright/test';

test.describe('Partner', () => {

    test('Partner anlegen und speichern', async ({ page }) => {
        await page.goto('/partner/new');

        await page.locator('input[name="company"]').fill('E2E Partner GmbH');
        await page.locator('input[name="name1"]').fill('E2E-Partner');

        await page.locator('[data-testid="btn-save"]').click();

        await expect(page).toHaveURL(/\/partner\/\d+\?saved=true/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Partner suchen und Ergebnisliste prüfen', async ({ page }) => {
        await page.goto('/partner/new');

        // QBE: company-Feld befüllen
        await page.locator('input[name="company"]').fill('Partner GmbH');
        await page.locator('[data-testid="btn-search"]').click();

        // Partner-Zeile 2001 in den Ergebnissen
        await expect(page.locator('[data-testid="partner-row-2001"]')).toBeVisible();
    });

    test('Partner aufrufen und zugeordnete Freiberufler sehen', async ({ page }) => {
        await page.goto('/partner/2001');

        // Seite lädt korrekt
        await expect(page).toHaveURL(/\/partner\/2001/);
        await expect(page.locator('input[name="company"]')).not.toBeEmpty();
    });

    test('Kontakthistorie bei Partner hinzufügen', async ({ page }) => {
        await page.goto('/partner/2001');

        const addHistoryBtn = page.locator('.list-hd button:has-text("Neuer Eintrag")').first();
        await addHistoryBtn.click();

        await page.locator('#history-description').fill('E2E-Partnernotiz');
        await page.locator('#btn-history-save').click();

        await page.locator('[data-testid="btn-save"]').click();
        await expect(page).toHaveURL(/saved=true/);
    });

});
