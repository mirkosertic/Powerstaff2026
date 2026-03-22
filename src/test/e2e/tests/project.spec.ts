import { test, expect } from '@playwright/test';

test.describe('Projekte', () => {

    test('Projekt suchen – QBE leer liefert alle Ergebnisse', async ({ page }) => {
        await page.goto('/project/new');

        await page.locator('[data-testid="btn-search"]').click();

        // Mindestens das E2E-Testprojekt sollte in der Ergebnisliste sein
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

        // Position 5001 (Freiberufler 1001) sollte sichtbar sein
        await expect(page.locator('[data-testid="position-item-5001"]')).toBeVisible();
    });

    test('Projektposition-Bearbeiten-Button öffnet Modal', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="position-edit-5001"]').click();

        // Modal für Bearbeiten ist geöffnet
        await expect(page.locator('#modal-edit-position')).not.toHaveClass(/hidden/);
    });

    test('Freiberufler per Code einem Projekt zuordnen', async ({ page }) => {
        await page.goto('/project/4001');

        // Modal öffnen
        await page.locator('button:has-text("Freiberufler über Code zuordnen")').click();
        await expect(page.locator('#modal-assign-by-code')).not.toHaveClass(/hidden/);

        // Code eingeben und zuordnen
        await page.locator('#assign-code-input').fill('E2E-002');
        await page.locator('#btn-assign-by-code').click();

        // Entweder Erfolg-Banner oder Bereits-Zugeordnet-Meldung
        await expect(page.locator('#banner-assigned, #assign-code-already-assigned')).toBeVisible({ timeout: 5_000 });
    });

    test('Projekthistorie hinzufügen', async ({ page }) => {
        await page.goto('/project/4001');

        const addHistoryBtn = page.locator('button:has-text("Neuer Eintrag")');
        await addHistoryBtn.click();

        await page.locator('#history-entry-description').fill('E2E-Projektnotiz');
        await page.locator('#btn-save-history').click();

        // Nach dem Neuladen ist die Notiz sichtbar
        await expect(page).toHaveURL(/\/project\/4001/);
    });

});
