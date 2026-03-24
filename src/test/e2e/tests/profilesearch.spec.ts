import { test, expect } from '@playwright/test';

test.describe('Profilsuche', () => {

    test('Profilsuche-Seite ist erreichbar', async ({ page }) => {
        // Redirect nach Login landet auf profilesearch
        await page.goto('/profilesearch');
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

    test('Nachricht senden startet Chat-Session', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        await page.locator('[data-testid="chat-input"]').fill('Suche Java-Entwickler in München');
        await page.locator('[data-testid="btn-chat-send"]').click();

        // Nach dem Senden: URL wechselt zur konkreten Chat-Session
        await expect(page).toHaveURL(/\/profilesearch\/chat\/\d+/, { timeout: 10_000 });
        await expect(page.locator('[data-testid="chat-messages"]')).toBeVisible();
    });

    test('Mock-Antwort erscheint nach Nachricht senden', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        // Nachricht senden
        await page.locator('[data-testid="chat-input"]').fill('Hallo Staffi');
        await page.locator('[data-testid="btn-chat-send"]').click();

        // Chat-URL abwarten
        await expect(page).toHaveURL(/\/profilesearch\/chat\/\d+/, { timeout: 10_000 });

        // Mock-Response muss im Chat erscheinen (MockLLmService gibt "Mock Response Nummer 0" zurück)
        await expect(page.locator('.msg-bubble').filter({ hasText: 'Mock Response Nummer 0' }))
            .toBeVisible({ timeout: 10_000 });
    });

    test('Benutzernachricht erscheint sofort im Chat', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        const userText = 'Testanfrage an den Mock-Assistenten';
        await page.locator('[data-testid="chat-input"]').fill(userText);
        await page.locator('[data-testid="btn-chat-send"]').click();

        // Benutzernachricht erscheint ohne Warten (wird sofort per JS gerendert)
        await expect(page.locator('.chat-msg.user .msg-bubble').filter({ hasText: userText }))
            .toBeVisible({ timeout: 5_000 });
    });

    test('Chat-Verlauf in Sidebar sichtbar', async ({ page }) => {
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\/chat\/(\d+)/);

        // Mindestens ein Chat-Session-Eintrag in der Sidebar
        await expect(page.locator('.chat-session-item').first()).toBeVisible();
    });

    test('Zweiten Chat anlegen und zwischen Chats navigieren', async ({ page }) => {
        // Ersten Chat mit einer Nachricht anlegen
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        await page.locator('[data-testid="chat-input"]').fill('Erster Chat');
        await page.locator('[data-testid="btn-chat-send"]').click();
        await expect(page).toHaveURL(/\/profilesearch\/chat\/(\d+)/, { timeout: 10_000 });
        const firstChatUrl = page.url();

        // Zweiten Chat anlegen
        await page.locator('button[type="submit"]:has-text("Neuer Chat")').click();
        await expect(page).toHaveURL(/\/profilesearch\/chat\/\d+/);
        const secondChatUrl = page.url();
        expect(secondChatUrl).not.toBe(firstChatUrl);

        // Zurück zum ersten Chat über die Sidebar
        const firstChatId = firstChatUrl.match(/\/profilesearch\/chat\/(\d+)/)?.[1];
        if (firstChatId) {
            await page.locator(`[data-testid="chat-session-${firstChatId}"]`).click();
            await expect(page).toHaveURL(new RegExp(`/profilesearch/chat/${firstChatId}`));
        }
    });

    test('Chat löschen leitet auf anderen Chat weiter', async ({ page }) => {
        // Chat anlegen und Nachricht senden
        await page.goto('/profilesearch');
        await expect(page).toHaveURL(/\/profilesearch\//);

        await page.locator('[data-testid="chat-input"]').fill('Chat zum Löschen');
        await page.locator('[data-testid="btn-chat-send"]').click();
        await expect(page).toHaveURL(/\/profilesearch\/chat\/\d+/, { timeout: 10_000 });

        const chatUrl = page.url();

        // Lösch-Modal öffnen und bestätigen
        await page.locator('button:has-text("Chat löschen")').click();
        await expect(page.locator('#modal-delete')).toBeVisible();
        await page.locator('#btn-confirm-delete').click();

        // Nach dem Löschen: Weiterleitung auf eine andere Profilsuche-Seite
        await expect(page).toHaveURL(/\/profilesearch\//, { timeout: 10_000 });
        await expect(page).not.toHaveURL(chatUrl);
    });

});
