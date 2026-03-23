import { test, expect } from '@playwright/test';

test.describe('Projekte', () => {

    test('Projekt suchen – QBE leer liefert alle Ergebnisse', async ({ page }) => {
        await page.goto('/project/new');

        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/project\/search/);

        await expect(page.locator('[data-testid="project-row-4001"]')).toBeVisible();
    });

    test('Projekt öffnen und Felder prüfen', async ({ page }) => {
        await page.goto('/project/4001');

        await expect(page).toHaveURL(/\/project\/4001/);
        await expect(page.locator('[data-testid="field-project-number"]')).toHaveValue('E2E-P001');
        await expect(page.locator('[data-testid="field-short-description"]')).toHaveValue('E2E Testprojekt');
    });

    test('Projektposition in Tabelle sichtbar', async ({ page }) => {
        await page.goto('/project/4001');

        await expect(page.locator('[data-testid="position-item-5001"]')).toBeVisible();
    });

    test('Projektposition-Bearbeiten-Button öffnet Modal', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="position-edit-5001"]').click();

        await expect(page.locator('#modal-edit-position')).not.toHaveClass(/hidden/);
    });

    test('Freiberufler per Code einem Projekt zuordnen', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="btn-assign-by-code-open"]').click();
        await expect(page.locator('#modal-assign-by-code')).not.toHaveClass(/hidden/);

        await page.locator('#modal-assign-by-code #assign-code-input').fill('E2E-002');
        await page.locator('#modal-assign-by-code #btn-assign-by-code').click();

        await expect(page.locator('#banner-assigned, #assign-code-already-assigned').first()).toBeVisible({ timeout: 5_000 });
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/project/search?projectNumber=E2E');
        await page.waitForURL(/\/project\/search/);

        // Seed hat nur 1 Treffer — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Projekthistorie hinzufügen', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="btn-add-history"]').click();
        await page.locator('#modal-history #history-entry-description').fill('E2E-Projektnotiz');
        await page.locator('#modal-history #btn-save-history').click();

        await expect(page).toHaveURL(/\/project\/4001/);
    });

});
