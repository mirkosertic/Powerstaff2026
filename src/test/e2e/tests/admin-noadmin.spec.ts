import { test, expect } from '@playwright/test';

test.describe('Administration – Nicht-Admin-Benutzer', () => {

    test.use({ storageState: undefined });

    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.getByTestId('field-username').fill('testuser-noadmin');
        await page.getByTestId('field-password').fill('testpass');
        await page.getByTestId('btn-login').click();
        await page.waitForURL('**/freelancer/**');
    });

    test('Nicht-Admin sieht in der Benutzerverwaltung nur sich selbst', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await expect(page.locator('[data-testid="user-row-testuser-noadmin"]')).toBeVisible();
        await expect(page.locator('[data-testid="user-row-testuser"]')).not.toBeAttached();
    });

    test('Nicht-Admin hat keine Bearbeiten- und Löschen-Buttons', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await expect(page.locator('[data-testid="btn-edit-user-testuser-noadmin"]')).not.toBeAttached();
        await expect(page.locator('[data-testid="btn-delete-user-testuser-noadmin"]')).not.toBeAttached();
        await expect(page.locator('[data-testid="btn-new-user"]')).not.toBeAttached();
    });

    test('Nicht-Admin kann eigenen Systemprompt bearbeiten', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await page.locator('[data-testid="btn-edit-prompt-testuser-noadmin"]').click();
        await expect(page.locator('#modal-edit-prompt')).not.toHaveClass(/hidden/);

        await page.locator('[data-testid="field-system-prompt"]').fill('Mein eigener Prompt');
        await page.locator('[data-testid="btn-submit-prompt"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        await expect(page.locator('.banner-success')).toBeVisible();
    });

    test('Nicht-Admin kann auf Admin-Stammdatenseiten nicht zugreifen (403)', async ({ page }) => {
        const response = await page.goto('/admin/historientypen');
        expect(response?.status()).toBe(403);
    });

});
