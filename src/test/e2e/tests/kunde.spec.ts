import { test, expect } from '@playwright/test';

test.describe('Kunden', () => {

    test('Kunden anlegen und speichern', async ({ page }) => {
        await page.goto('/kunde/new');

        await page.locator('input[name="company"]').fill('E2E Kunde AG');
        await page.locator('input[name="name1"]').fill('E2E-Kunde');

        await page.locator('[data-testid="btn-save"]').click();

        await expect(page).toHaveURL(/\/kunde\/\d+\?saved=true/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Kunden suchen und Ergebnisliste prüfen', async ({ page }) => {
        await page.goto('/kunde/new');

        await page.locator('input[name="company"]').fill('Kunde AG');
        await page.locator('[data-testid="btn-search"]').click();

        await expect(page.locator('[data-testid="kunde-row-3001"]')).toBeVisible();
    });

    test('Kunden öffnen und Projektliste sehen', async ({ page }) => {
        await page.goto('/kunde/3001');

        await expect(page).toHaveURL(/\/kunde\/3001/);
        // Projekt E2E-P001 sollte in der Projektliste erscheinen
        await expect(page.locator('[data-testid="kunde-project-row-4001"]')).toBeVisible();
    });

    test('Klick auf Projekt in Kundenliste navigiert zur Projektseite', async ({ page }) => {
        await page.goto('/kunde/3001');

        await page.locator('[data-testid="kunde-project-row-4001"]').click();
        await expect(page).toHaveURL(/\/project\/4001/);
    });

});
