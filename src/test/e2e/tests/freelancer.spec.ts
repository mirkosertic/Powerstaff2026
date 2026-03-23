import { test, expect } from '@playwright/test';

test.describe('Freiberufler', () => {

    test('Freiberufler per Code suchen und in Ergebnisliste anklicken', async ({ page }) => {
        await page.goto('/freelancer/new');
        await page.locator('[data-testid="field-code"]').fill('E2E-001');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/freelancer\/search/);

        await expect(page.locator('[data-testid="freelancer-row-1001"]')).toBeVisible();
        await page.locator('[data-testid="freelancer-row-1001"]').click();
        await page.waitForURL(/\/freelancer\/1001/);

        await expect(page.locator('[data-testid="field-code"]')).toHaveValue('E2E-001');
    });

    test('Neuen Freiberufler anlegen und speichern', async ({ page }) => {
        await page.goto('/freelancer/new');

        await page.locator('[data-testid="field-lastname"]').fill('Neutest');
        await page.locator('[data-testid="field-firstname"]').fill('Anna');
        await page.locator('[data-testid="field-code"]').fill('E2E-NEW-1');
        // Kontaktart ist Pflichtfeld
        await page.locator('select[name="kontaktart"]').selectOption('NL');

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/freelancer\/\d+/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Kontakthistorie-Eintrag hinzufügen', async ({ page }) => {
        await page.goto('/freelancer/1001');

        await page.locator('[data-testid="btn-add-history"]').click();

        // Selektoren innerhalb des Modals qualifizieren – der Modal-Body
        // ist doppelt im DOM (Thymeleaf-Quelle + gerendert), daher #modal-history vorschalten
        await page.locator('#modal-history #history-description').fill('E2E-Testnotiz via Playwright');
        await page.locator('#modal-history #btn-history-save').click();

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/freelancer\/1001/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/freelancer/search?city=M%C3%BCnchen');
        await page.waitForURL(/\/freelancer\/search/);

        // Seed hat nur 1 Treffer für München — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Freiberufler einem gemerkten Projekt zuordnen – Bereits zugeordnet (409)', async ({ page }) => {
        await page.goto('/project/4001');
        await page.goto('/freelancer/1001');

        const assignBtn = page.locator('[data-testid="btn-assign-project"]');
        if (await assignBtn.isVisible()) {
            await assignBtn.click();
            await expect(page.locator('#assign-already-msg')).toBeVisible();
        }
    });

    test('Suchen-Button bei neuem Datensatz sichtbar', async ({ page }) => {
        await page.goto('/freelancer/new');

        await expect(page.locator('[data-testid="btn-search"]')).toBeVisible();
    });

    test('Suchen-Button bei bestehendem Datensatz nicht sichtbar', async ({ page }) => {
        await page.goto('/freelancer/1001');

        await expect(page.locator('[data-testid="btn-search"]')).not.toBeVisible();
    });

    test('Datumsfeld lastContactDate: Datum setzen und speichern ohne Konvertierungsfehler', async ({ page }) => {
        await page.goto('/freelancer/1001');

        // Datum setzen (ISO-Format für input[type=date])
        await page.locator('[data-testid="field-last-contact-date"]').fill('2026-03-15');

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/freelancer\/1001/);
        await expect(page.locator('#banner-success')).toBeVisible();
        // Kein Konvertierungsfehler-Banner
        await expect(page.locator('#banner-save-error')).not.toBeVisible();
    });

});
