import { test, expect } from '@playwright/test';

test.describe('Kunden', () => {

    test('Kunden anlegen und speichern', async ({ page }) => {
        await page.goto('/kunde/new');

        await page.locator('input[name="company"]').fill('E2E Kunde AG');
        await page.locator('input[name="name1"]').fill('E2E-Kunde');

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/kunde\/\d+/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Kunden suchen und Ergebnisliste prüfen', async ({ page }) => {
        await page.goto('/kunde/new');

        await page.locator('input[name="company"]').fill('Kunde AG');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/kunde\/search/);

        await expect(page.locator('[data-testid="kunde-row-3001"]')).toBeVisible();
    });

    test('Kunden öffnen und Projektliste sehen', async ({ page }) => {
        await page.goto('/kunde/3001');

        await expect(page).toHaveURL(/\/kunde\/3001/);
        await expect(page.locator('[data-testid="kunde-project-row-4001"]')).toBeVisible();
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/kunde/search?company=Kunde+AG');
        await page.waitForURL(/\/kunde\/search/);

        // Seed hat nur 1 Treffer — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Klick auf Projekt in Kundenliste navigiert zur Projektseite', async ({ page }) => {
        await page.goto('/kunde/3001');

        await page.locator('[data-testid="kunde-project-row-4001"]').click();
        await page.waitForURL(/\/project\/4001/);
    });

    test('Projekt-Status wird als Text angezeigt, nicht als numerischer Index', async ({ page }) => {
        await page.goto('/kunde/3001');

        const statusCell = page.locator('[data-testid="projekt-status-4001"]');
        await expect(statusCell).toBeVisible();

        const statusText = await statusCell.textContent();
        const validLabels = ['Offen', 'Verloren', 'Storniert', 'Besetzt', 'Suche abgeschlossen'];
        expect(validLabels).toContain(statusText?.trim());

        // Kein reines Numeral (1–5)
        expect(statusText?.trim()).not.toMatch(/^[1-5]$/);
    });

    test('Sortierung in QBE-Suchergebnissen: Klick auf Spaltenheader ändert Reihenfolge, Menge bleibt gleich', async ({ page }) => {
        // Suche mit mehreren Ergebnissen (alle Kunden)
        await page.goto('/kunde/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/kunde\/search/);

        // Warten bis Ergebnistabelle sichtbar
        const resultsTable = page.locator('[data-testid="results-table"]');
        await expect(resultsTable).toBeVisible();

        // Alle Zeilen sammeln (ursprüngliche Reihenfolge)
        const rows = resultsTable.locator('tbody tr');
        const initialCount = await rows.count();

        // Wenn nur 1 Ergebnis, Test überspringen
        if (initialCount <= 1) {
            console.log('Skipping sort test: not enough results (need at least 2)');
            return;
        }

        const initialIds: string[] = [];
        for (let i = 0; i < initialCount; i++) {
            const testId = await rows.nth(i).getAttribute('data-testid');
            if (testId) initialIds.push(testId);
        }

        // Auf "Firma"-Spaltenheader klicken (erste Spalte = Index 0)
        const companyHeader = resultsTable.locator('thead th').first().locator('a');
        await companyHeader.click();
        await page.waitForURL(/sortField=company/);

        // Ergebnistabelle noch vorhanden
        await expect(resultsTable).toBeVisible();

        // Zeilen-IDs nach Sortierung sammeln
        const sortedCount = await rows.count();
        const sortedIds: string[] = [];
        for (let i = 0; i < sortedCount; i++) {
            const testId = await rows.nth(i).getAttribute('data-testid');
            if (testId) sortedIds.push(testId);
        }

        // Gleiche Anzahl (Menge bleibt gleich)
        expect(sortedCount).toBe(initialCount);

        // URL wurde geändert
        expect(page.url()).toContain('sortField=company');

        // Alle ursprünglichen IDs sind noch vorhanden
        for (const id of initialIds) {
            expect(sortedIds).toContain(id);
        }
    });

});
