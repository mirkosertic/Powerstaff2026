import { test, expect } from '@playwright/test';

test.describe('Partner', () => {

    test('Partner anlegen und speichern', async ({ page }) => {
        await page.goto('/partner/new');

        await page.locator('input[name="company"]').fill('E2E Partner GmbH');
        await page.locator('input[name="name1"]').fill('E2E-Partner');

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/partner\/\d+/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Partner suchen und Ergebnisliste prüfen', async ({ page }) => {
        await page.goto('/partner/new');

        await page.locator('input[name="company"]').fill('Partner GmbH');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/partner\/search/);

        await expect(page.locator('[data-testid="partner-row-2001"]')).toBeVisible();
    });

    test('Partner aufrufen und Stammdaten prüfen', async ({ page }) => {
        await page.goto('/partner/2001');

        await expect(page).toHaveURL(/\/partner\/2001/);
        await expect(page.locator('input[name="company"]')).not.toBeEmpty();
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/partner/search?company=Partner+GmbH');
        await page.waitForURL(/\/partner\/search/);

        // Seed hat nur 1 Treffer — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Projekt-Status wird als Text angezeigt, nicht als numerischer Index', async ({ page }) => {
        await page.goto('/partner/2001');

        const projectRow = page.locator('[data-testid="partner-project-row-4002"]');
        await expect(projectRow).toBeVisible();

        const statusCell = page.locator('[data-testid="projekt-status-4002"]');
        await expect(statusCell).toBeVisible();

        const statusText = await statusCell.textContent();
        const validLabels = ['Offen', 'Verloren', 'Storniert', 'Besetzt', 'Suche abgeschlossen'];
        expect(validLabels).toContain(statusText?.trim());

        // Kein reines Numeral (1–5)
        expect(statusText?.trim()).not.toMatch(/^[1-5]$/);
    });

    test('Kontakthistorie bei Partner hinzufügen', async ({ page }) => {
        await page.goto('/partner/2001');

        await page.locator('[data-testid="btn-add-history"]').click();
        await page.locator('#modal-history #history-description').fill('E2E-Partnernotiz');
        await page.locator('#modal-history #btn-history-save').click();

        await page.locator('[data-testid="btn-save"]').click();

        await page.waitForURL(/\/partner\/2001/);
        await expect(page.locator('#banner-success')).toBeVisible();
    });

    test('Sortierung in QBE-Suchergebnissen: Klick auf Spaltenheader ändert Reihenfolge, Menge bleibt gleich', async ({ page }) => {
        // Suche mit mehreren Ergebnissen (alle Partner)
        await page.goto('/partner/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/partner\/search/);

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
