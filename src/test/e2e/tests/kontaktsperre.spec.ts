import { test, expect } from '@playwright/test';

test.describe('Kontaktsperre – Rot-Markierung in Suchergebnislisten', () => {

    // =========================================================================
    // Freiberufler-Suche
    // =========================================================================

    test('Freiberufler-Suche: Zeile mit Kontaktsperre hat CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/freelancer/search?name1=Gesperrt');
        await page.waitForURL(/\/freelancer\/search/);
        const row = page.locator('[data-testid="freelancer-row-1004"]');
        await expect(row).toBeVisible();
        await expect(row).toHaveClass(/row-forbidden/);
    });

    test('Freiberufler-Suche: Zeile ohne Kontaktsperre hat KEINE CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/freelancer/search?name1=Mustermann');
        await page.waitForURL(/\/freelancer\/search/);
        const row = page.locator('[data-testid="freelancer-row-1001"]');
        await expect(row).toBeVisible();
        await expect(row).not.toHaveClass(/row-forbidden/);
    });

    // =========================================================================
    // Partner-Suche
    // =========================================================================

    test('Partner-Suche: Zeile mit Kontaktsperre hat CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/partner/search?name1=Gesperrt');
        await page.waitForURL(/\/partner\/search/);
        const row = page.locator('[data-testid="partner-row-2002"]');
        await expect(row).toBeVisible();
        await expect(row).toHaveClass(/row-forbidden/);
    });

    test('Partner-Suche: Zeile ohne Kontaktsperre hat KEINE CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/partner/search?name1=Partner');
        await page.waitForURL(/\/partner\/search/);
        const row = page.locator('[data-testid="partner-row-2001"]');
        await expect(row).toBeVisible();
        await expect(row).not.toHaveClass(/row-forbidden/);
    });

    // =========================================================================
    // Kunden-Suche
    // =========================================================================

    test('Kunden-Suche: Zeile mit Kontaktsperre hat CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/kunde/search?name1=Gesperrt');
        await page.waitForURL(/\/kunde\/search/);
        const row = page.locator('[data-testid="kunde-row-3002"]');
        await expect(row).toBeVisible();
        await expect(row).toHaveClass(/row-forbidden/);
    });

    test('Kunden-Suche: Zeile ohne Kontaktsperre hat KEINE CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/kunde/search?name1=Kunde');
        await page.waitForURL(/\/kunde\/search/);
        const row = page.locator('[data-testid="kunde-row-3001"]');
        await expect(row).toBeVisible();
        await expect(row).not.toHaveClass(/row-forbidden/);
    });

    // =========================================================================
    // Projektpositionen
    // =========================================================================

    test('Projektpositionen: Zeile mit Kontaktsperre hat CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/project/4001');
        await page.waitForURL(/\/project\/4001/);
        const row = page.locator('[data-testid="position-item-5002"]');
        await expect(row).toBeVisible();
        await expect(row).toHaveClass(/row-forbidden/);
    });

    test('Projektpositionen: Zeile ohne Kontaktsperre hat KEINE CSS-Klasse row-forbidden', async ({ page }) => {
        await page.goto('/project/4001');
        await page.waitForURL(/\/project\/4001/);
        const row = page.locator('[data-testid="position-item-5001"]');
        await expect(row).toBeVisible();
        await expect(row).not.toHaveClass(/row-forbidden/);
    });

});
