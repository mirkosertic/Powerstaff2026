import { test, expect } from '@playwright/test';
import * as path from 'path';

const SCREENSHOTS = path.resolve(__dirname, '../../../../docs/screenshots');

// Hilfsfunktion: navigiert zur ersten Zeile der Ergebnistabelle und klickt sie an.
// Gibt die URL nach dem Klick zurück.
async function openFirstResult(page: any, urlPattern: RegExp): Promise<void> {
    const firstRow = page.locator('[data-testid="results-table"] tbody tr').first();
    await expect(firstRow).toBeVisible({ timeout: 10_000 });
    await firstRow.click();
    await page.waitForURL(urlPattern);
}

test.describe('Handbuch-Screenshots', () => {

    test('Login-Seite', async ({ page }) => {
        // Direkt ohne Auth-State aufrufen
        await page.context().clearCookies();
        await page.goto('/login', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/login.png`, fullPage: true });
    });

    test('Freiberufler – Suchmaske', async ({ page }) => {
        await page.goto('/freelancer/new', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/freiberufler-suchmaske.png`, fullPage: true });
    });

    test('Freiberufler – Suchergebnisse', async ({ page }) => {
        await page.goto('/freelancer/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/freelancer\/search/);
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
        await page.screenshot({ path: `${SCREENSHOTS}/freiberufler-suchergebnisse.png`, fullPage: true });
    });

    test('Freiberufler – Formular (bestehender Datensatz)', async ({ page }) => {
        await page.goto('/freelancer/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/freelancer\/search/);
        await openFirstResult(page, /\/freelancer\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/freiberufler-formular.png`, fullPage: true });
    });

    test('Partner – Suchmaske', async ({ page }) => {
        await page.goto('/partner/new', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/partner-suchmaske.png`, fullPage: true });
    });

    test('Partner – Suchergebnisse', async ({ page }) => {
        await page.goto('/partner/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/partner\/search/);
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
        await page.screenshot({ path: `${SCREENSHOTS}/partner-suchergebnisse.png`, fullPage: true });
    });

    test('Partner – Formular (bestehender Datensatz)', async ({ page }) => {
        await page.goto('/partner/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/partner\/search/);
        await openFirstResult(page, /\/partner\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/partner-formular.png`, fullPage: true });
    });

    test('Kunden – Suchmaske', async ({ page }) => {
        await page.goto('/kunde/new', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/kunden-suchmaske.png`, fullPage: true });
    });

    test('Kunden – Suchergebnisse', async ({ page }) => {
        await page.goto('/kunde/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/kunde\/search/);
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
        await page.screenshot({ path: `${SCREENSHOTS}/kunden-suchergebnisse.png`, fullPage: true });
    });

    test('Kunden – Formular (bestehender Datensatz)', async ({ page }) => {
        await page.goto('/kunde/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/kunde\/search/);
        await openFirstResult(page, /\/kunde\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/kunden-formular.png`, fullPage: true });
    });

    test('Projekte – Suchmaske', async ({ page }) => {
        await page.goto('/project/new', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/projekte-suchmaske.png`, fullPage: true });
    });

    test('Projekte – Suchergebnisse', async ({ page }) => {
        await page.goto('/project/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/project\/search/);
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
        await page.screenshot({ path: `${SCREENSHOTS}/projekte-suchergebnisse.png`, fullPage: true });
    });

    test('Projekte – Formular mit Positionen', async ({ page }) => {
        await page.goto('/project/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/project\/search/);
        await openFirstResult(page, /\/project\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/projekte-formular.png`, fullPage: true });
    });

    test('Profilsuche – KI-Chat', async ({ page }) => {
        await page.goto('/profilesearch/new', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/profilsuche-ki-chat.png`, fullPage: true });
    });

    test('Profilsuche – Klassische Suche Suchmaske', async ({ page }) => {
        await page.goto('/profilesearch/search', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/profilsuche-klassisch-suchmaske.png`, fullPage: true });
    });

    test('Profilsuche – Klassische Suche Ergebnisse', async ({ page }) => {
        await page.goto('/profilesearch/search');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search-results/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/profilsuche-klassisch-ergebnisse.png`, fullPage: true });
    });

    test('Admin – Historientypen', async ({ page }) => {
        await page.goto('/admin/historientypen', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/admin-historientypen.png`, fullPage: true });
    });

    test('Admin – Positionsstatus', async ({ page }) => {
        await page.goto('/admin/positionsstatus', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/admin-positionsstatus.png`, fullPage: true });
    });

    test('Admin – Tags', async ({ page }) => {
        await page.goto('/admin/tags', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/admin-tags.png`, fullPage: true });
    });

    test('Admin – Benutzerverwaltung', async ({ page }) => {
        await page.goto('/admin/users', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/admin-benutzer.png`, fullPage: true });
    });

});
