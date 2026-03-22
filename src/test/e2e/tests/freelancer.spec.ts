import { test, expect } from '@playwright/test';

test.describe('Freiberufler', () => {

    test('Freiberufler per Code suchen und in Ergebnisliste anklicken', async ({ page }) => {
        await page.goto('/freelancer/new');
        // QBE-Suche über code-Feld
        await page.locator('[data-testid="field-code"]').fill('E2E-001');
        await page.locator('[data-testid="btn-search"]').click();

        // Ergebnisseite: Zeile für Freelancer 1001
        await expect(page.locator('[data-testid="freelancer-row-1001"]')).toBeVisible();
        await page.locator('[data-testid="freelancer-row-1001"]').click();

        // Formular des Freiberuflers sollte geladen sein
        await expect(page).toHaveURL(/\/freelancer\/1001/);
        await expect(page.locator('[data-testid="field-code"]')).toHaveValue('E2E-001');
    });

    test('Neuen Freiberufler anlegen und speichern', async ({ page }) => {
        await page.goto('/freelancer/new');

        await page.locator('[data-testid="field-lastname"]').fill('Neutest');
        await page.locator('[data-testid="field-firstname"]').fill('Anna');
        await page.locator('[data-testid="field-code"]').fill('E2E-NEW-1');

        await page.locator('[data-testid="btn-save"]').click();

        // Nach dem Speichern: URL enthält neue ID, Banner sichtbar
        await expect(page).toHaveURL(/\/freelancer\/\d+\?saved=true/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Kontakthistorie-Eintrag hinzufügen', async ({ page }) => {
        await page.goto('/freelancer/1001');

        await page.locator('[data-testid="btn-add-history"]').click();

        // Modal öffnen und Beschreibung eingeben
        await page.locator('#history-description').fill('E2E-Testnotiz via Playwright');
        await page.locator('#btn-history-save').click();

        // Gespeichert-Banner nach dem Speichern des Formulars
        await page.locator('[data-testid="btn-save"]').click();
        await expect(page).toHaveURL(/saved=true/);
    });

    test('Freiberufler einem gemerkten Projekt zuordnen – Bereits zugeordnet (409)', async ({ page }) => {
        // Projekt merken
        await page.goto('/project/4001');

        // Freiberufler öffnen, der bereits zugeordnet ist
        await page.goto('/freelancer/1001');

        // Zuordnen-Button klicken (Freiberufler ist bereits in Projekt 4001)
        const assignBtn = page.locator('[data-testid="btn-assign-project"]');
        if (await assignBtn.isVisible()) {
            await assignBtn.click();
            await expect(page.locator('#assign-already-msg')).toBeVisible();
        }
    });

});
