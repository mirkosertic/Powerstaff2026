import { test, expect } from '@playwright/test';

// Admin-Benutzer: testuser / testpass (is_admin = TRUE, Seed: V100__e2e_seed.sql)
// Nicht-Admin:    testuser-noadmin / testpass (is_admin = FALSE)

test.describe('API-Token-Verwaltung – Admin', () => {

    test('Admin sieht die LLM-API-Token-Spalte und den Schlüssel-Button', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Spaltenüberschrift vorhanden
        await expect(page.locator('th').filter({ hasText: 'LLM API-Token' })).toBeVisible();

        // Schlüssel-Button für testuser vorhanden
        await expect(page.locator('[data-testid="btn-edit-apitoken-testuser"]')).toBeVisible();
    });

    test('Admin kann Token für einen Benutzer setzen – Badge wechselt zu "Gesetzt"', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Sicherstellen, dass Token aktuell nicht gesetzt ist (Reset: leeres Token senden)
        await page.locator('[data-testid="btn-edit-apitoken-testuser"]').click();
        await expect(page.locator('#modal-edit-apitoken')).not.toHaveClass(/hidden/);
        await page.locator('[data-testid="field-apitoken"]').fill('');
        await page.locator('[data-testid="btn-submit-apitoken"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        // Jetzt Token setzen
        await page.locator('[data-testid="btn-edit-apitoken-testuser"]').click();
        await expect(page.locator('#modal-edit-apitoken')).not.toHaveClass(/hidden/);
        await page.locator('[data-testid="field-apitoken"]').fill('test-api-token-e2e-12345');
        await page.locator('[data-testid="btn-submit-apitoken"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        await expect(page.locator('.banner-success')).toBeVisible();
        await expect(page.locator('[data-testid="apitoken-set-testuser"]')).toBeVisible();
    });

    test('Admin kann Token für einen Benutzer löschen – Badge wechselt zu "Nicht gesetzt"', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Token leeren
        await page.locator('[data-testid="btn-edit-apitoken-testuser"]').click();
        await expect(page.locator('#modal-edit-apitoken')).not.toHaveClass(/hidden/);
        await page.locator('[data-testid="field-apitoken"]').fill('');
        await page.locator('[data-testid="btn-submit-apitoken"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        await expect(page.locator('.banner-success')).toBeVisible();
        await expect(page.locator('[data-testid="apitoken-unset-testuser"]')).toBeVisible();
    });

    test('Das Modal-Eingabefeld ist beim Öffnen immer leer (kein Token im DOM)', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Erst Token setzen
        await page.locator('[data-testid="btn-edit-apitoken-testuser"]').click();
        await page.locator('[data-testid="field-apitoken"]').fill('secret-token-xyz');
        await page.locator('[data-testid="btn-submit-apitoken"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        // Modal erneut öffnen – Feld muss leer sein
        await page.locator('[data-testid="btn-edit-apitoken-testuser"]').click();
        await expect(page.locator('[data-testid="field-apitoken"]')).toHaveValue('');

        // Roher Token-Wert darf nirgendwo im HTML stehen
        const html = await page.content();
        expect(html).not.toContain('secret-token-xyz');

        // Aufräumen
        await page.locator('[data-testid="field-apitoken"]').fill('');
        await page.locator('[data-testid="btn-submit-apitoken"]').click();
        await page.waitForURL(/\/admin\/benutzer/);
    });

});
