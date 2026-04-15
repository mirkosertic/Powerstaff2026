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

test.describe('API-Token-Verwaltung – Nicht-Admin', () => {

    test.use({ storageState: undefined });

    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.getByTestId('field-username').fill('testuser-noadmin');
        await page.getByTestId('field-password').fill('testpass');
        await page.getByTestId('btn-login').click();
        await page.waitForURL('**/freelancer/**');
    });

    test('Nicht-Admin sieht keine LLM-API-Token-Spalte', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await expect(page.locator('th').filter({ hasText: 'LLM API-Token' })).not.toBeAttached();
    });

    test('Nicht-Admin sieht keinen Schlüssel-Button für den eigenen Eintrag', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await expect(page.locator('[data-testid="btn-edit-apitoken-testuser-noadmin"]')).not.toBeAttached();
    });

    test('Nicht-Admin erhält Fehlermeldung beim direkten POST auf den Endpoint', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // CSRF-Token aus dem Meta-Tag lesen
        const csrfToken = await page.evaluate(() =>
            (document.querySelector('meta[name="csrf-token"]') as HTMLMetaElement)?.content ?? ''
        );

        // Direkt POST auf den geschützten Endpoint (kein UI-Weg für Non-Admin)
        const response = await page.request.post('/admin/benutzer/testuser-noadmin/apitoken', {
            headers: { 'X-XSRF-TOKEN': csrfToken },
            form: { llmApiToken: 'hacker-token' },
        });

        // Controller leitet weiter auf /admin/benutzer mit Flash-Fehler
        // Nach dem Redirect-Follow enthält die Seite den Fehler-Banner
        const body = await response.text();
        expect(body).toContain('Keine Berechtigung');
    });

});
