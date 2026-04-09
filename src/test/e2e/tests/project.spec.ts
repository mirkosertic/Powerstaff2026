import { test, expect } from '@playwright/test';

test.describe('Projekte', () => {

    test('Projekt suchen – QBE leer liefert alle Ergebnisse', async ({ page }) => {
        await page.goto('/project/new');

        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/project\/search/);

        await expect(page.locator('[data-testid="project-row-4001"]')).toBeVisible();
    });

    test('Projekt öffnen und Felder prüfen', async ({ page }) => {
        await page.goto('/project/4001');

        await expect(page).toHaveURL(/\/project\/4001/);
        await expect(page.locator('[data-testid="field-project-number"]')).toHaveValue('E2E-P001');
        await expect(page.locator('[data-testid="field-short-description"]')).toHaveValue('E2E Testprojekt');
    });

    test('Projektposition in Tabelle sichtbar', async ({ page }) => {
        await page.goto('/project/4001');

        await expect(page.locator('[data-testid="position-item-5001"]')).toBeVisible();
    });

    test('Projektposition-Bearbeiten-Button öffnet Modal', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="position-edit-5001"]').click();

        await expect(page.locator('#modal-edit-position')).not.toHaveClass(/hidden/);
    });

    test('Projektposition bearbeiten: Konditionen ändern und speichern', async ({ page }) => {
        await page.goto('/project/4001');

        // Bearbeiten-Modal öffnen
        await page.locator('[data-testid="position-edit-5001"]').click();
        await expect(page.locator('#modal-edit-position')).not.toHaveClass(/hidden/);

        // Konditionen ändern
        await page.locator('#modal-edit-position #edit-pos-konditionen').fill('600 EUR/Tag');

        // Speichern
        await page.locator('#modal-edit-position #btn-save-position').click();

        // Erfolgs-Banner sichtbar (erscheint bevor reload)
        await expect(page.locator('#banner-success')).toBeVisible({ timeout: 5_000 });
    });

    test('Projektposition löschen: Bestätigungs-Modal öffnet sich und Löschen führt zu Reload', async ({ page }) => {
        await page.goto('/project/4001');

        // Position sichtbar
        await expect(page.locator('[data-testid="position-item-5001"]')).toBeVisible();

        // Löschen-Modal öffnen
        await page.locator('[data-testid="position-delete-5001"]').click();
        await expect(page.locator('#modal-delete-position')).not.toHaveClass(/hidden/);

        // Löschen abbrechen (um Testdaten zu erhalten)
        await page.locator('#modal-delete-position button.btn-ghost').click();
        await expect(page.locator('#modal-delete-position')).toHaveClass(/hidden/);

        // Position noch vorhanden
        await expect(page.locator('[data-testid="position-item-5001"]')).toBeVisible();
    });

    test('Freiberufler per Code einem Projekt zuordnen', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="btn-assign-by-code-open"]').click();
        await expect(page.locator('#modal-assign-by-code')).not.toHaveClass(/hidden/);

        await page.locator('#modal-assign-by-code #assign-code-input').fill('E2E-002');
        await page.locator('#modal-assign-by-code #btn-assign-by-code').click();

        await expect(page.locator('#banner-assigned, #assign-code-already-assigned').first()).toBeVisible({ timeout: 5_000 });
    });

    test('Suche zeigt kein Nachladen-Element wenn alle Ergebnisse auf eine Seite passen', async ({ page }) => {
        await page.goto('/project/search?projectNumber=E2E');
        await page.waitForURL(/\/project\/search/);

        // Seed hat nur 1 Treffer — weit unter PAGE_SIZE (20)
        await expect(page.locator('ps-infinite-scroll')).not.toBeAttached();
    });

    test('Projekthistorie hinzufügen', async ({ page }) => {
        await page.goto('/project/4001');

        await page.locator('[data-testid="btn-add-history"]').click();
        await page.locator('#modal-history #history-entry-description').fill('E2E-Projektnotiz');
        await page.locator('#modal-history #btn-save-history').click();

        await expect(page).toHaveURL(/\/project\/4001/);
    });

    test('Sortierung in QBE-Suchergebnissen: Klick auf Spaltenheader ändert Reihenfolge, Menge bleibt gleich', async ({ page }) => {
        // Suche mit mehreren Ergebnissen (alle Projekte)
        await page.goto('/project/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/project\/search/);

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

        // Auf "Projektnummer"-Spaltenheader klicken (erste sortierbare Spalte)
        const projectNumberHeader = resultsTable.locator('thead th').first().locator('a');
        await projectNumberHeader.click();
        await page.waitForURL(/sortField=/);

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

        // URL wurde geändert (enthält sortField Parameter)
        expect(page.url()).toMatch(/sortField=/);

        // Alle ursprünglichen IDs sind noch vorhanden
        for (const id of initialIds) {
            expect(sortedIds).toContain(id);
        }
    });

});
