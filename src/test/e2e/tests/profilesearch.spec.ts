import { test, expect } from '@playwright/test';

test.describe('Profilsuche', () => {

    test('Profilsuche-Seite ist erreichbar', async ({ page }) => {
        // Redirect nach Login landet auf profilesearch
        const response = await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);
        await expect(page.locator('[data-testid="chat-messages"]')).toBeVisible();
    });

    test('Chat-Eingabefeld und Senden-Button vorhanden', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        await expect(page.locator('[data-testid="chat-input"]')).toBeVisible();
        await expect(page.locator('[data-testid="btn-chat-send"]')).toBeVisible();
    });

    test('Neuen Chat anlegen', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        const newChatBtn = page.locator('button[type="submit"]:has-text("Neuer Chat")');
        await newChatBtn.click();

        // URL sollte auf neuen Chat zeigen
        await expect(page).toHaveURL(/\/profilesearch\/chat\/\d+/);
        await expect(page.locator('[data-testid="chat-input"]')).toBeVisible();
    });

    test('Nachricht senden und Stub-Antwort erhalten', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        await page.locator('[data-testid="chat-input"]').fill('Suche Java-Entwickler in München');
        await page.locator('[data-testid="btn-chat-send"]').click();

        // Stub-Antwort sollte in Nachrichten-Container erscheinen
        await expect(page.locator('[data-testid="chat-messages"]')).toContainText('KI-Profilsuche', { timeout: 15_000 });
    });

    test('Chat-Verlauf in Sidebar sichtbar', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\/chat\/(\d+)/);

        // Mindestens ein Chat-Session-Eintrag in der Sidebar
        await expect(page.locator('.chat-session-item').first()).toBeVisible();
    });

});
