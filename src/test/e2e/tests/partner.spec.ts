import { test, expect } from '@playwright/test';

test.describe('Partner', () => {

    test('Partner anlegen und speichern', async ({ page }) => {
        await page.goto('/partner/new');

        await page.locator('input[name="company"]').fill('E2E Partner GmbH');
        await page.locator('input[name="name1"]').fill('E2E-Partner');

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/partner\/\d+/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Partner suchen und Ergebnisliste prüfen', async ({ page }) => {
        await page.goto('/partner/new');

        await page.locator('input[name="company"]').fill('Partner GmbH');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/partner\/search/);

        await expect(page.locator('[data-testid="partner-row-2001"]')).toBeVisible();
    });

    test('Partner aufrufen und Stammdaten prüfen', async ({ page }) => {
        await page.goto('/partner/2001');

        await expect(page).toHaveURL(/\/partner\/2001/);
        await expect(page.locator('input[name="company"]')).not.toBeEmpty();
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/partner/search?company=Partner+GmbH');
        await page.waitForURL(/\/partner\/search/);

        // Seed hat nur 1 Treffer — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Kontakthistorie bei Partner hinzufügen', async ({ page }) => {
        await page.goto('/partner/2001');

        await page.locator('[data-testid="btn-add-history"]').click();
        await page.locator('#modal-history #history-description').fill('E2E-Partnernotiz');
        await page.locator('#modal-history #btn-history-save').click();

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/partner\/2001/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

});
