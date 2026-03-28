import { test, expect } from '@playwright/test';

test.describe('Profilsuche – Klassische Suche', () => {

    // =========================================================================
    // Szenario 1: Tab-Navigation
    // =========================================================================

    test('Tab-Navigation: Suche-Tab auf Chat-Seite sichtbar und leitet auf /profilesearch/search weiter', async ({ page }) => {
        // Arrange: Chat-Seite öffnen
        await page.goto('/profilesearch');
        await page.waitForURL(/\/profilesearch\/chat\/\d+/);

        // Assert: Beide Tab-Buttons vorhanden
        await expect(page.locator('[data-testid="tab-chat"]')).toBeVisible();
        await expect(page.locator('[data-testid="tab-search"]')).toBeVisible();

        // Act: Auf "Suche"-Tab klicken
        await page.locator('[data-testid="tab-search"]').click();
        await page.waitForURL(/\/profilesearch\/search/);

        // Assert: Suchseite geladen, Suchformular vorhanden
        await expect(page.locator('[data-testid="input-search-term"]')).toBeVisible();
        await expect(page.locator('[data-testid="btn-search"]')).toBeVisible();
    });

    test('Tab-Navigation: Chat-Tab auf Suchseite sichtbar und leitet auf /profilesearch/chat weiter', async ({ page }) => {
        // Arrange: Suchseite direkt öffnen (mit Suchbegriff, damit Formular valide ist)
        await page.goto('/profilesearch/search?searchTerm=Mock');
        await page.waitForURL(/\/profilesearch\/search/);

        // Assert: Beide Tab-Buttons vorhanden, Chat ist ghost (nicht aktiv), Suche ist pri (aktiv)
        await expect(page.locator('[data-testid="tab-chat"]')).toBeVisible();
        await expect(page.locator('[data-testid="tab-search"]')).toBeVisible();

        // Act: Auf "Chat"-Tab klicken
        await page.locator('[data-testid="tab-chat"]').click();
        await page.waitForURL(/\/profilesearch\/chat\/\d+/);

        // Assert: Chat-Eingabe vorhanden
        await expect(page.locator('[data-testid="chat-input"]')).toBeVisible();
    });

    // =========================================================================
    // Szenario 2: Suche ohne Kriterien → Validierungsfehler
    // =========================================================================

    test('Suche ohne Kriterien zeigt Validierungsfehler-Banner', async ({ page }) => {
        // Arrange: Suchseite ohne Parameter aufrufen
        await page.goto('/profilesearch/search');

        // Assert: Validierungsfehler-Banner vorhanden
        await expect(page.locator('[data-testid="validation-error-banner"]')).toBeVisible();

        // Assert: Ergebnistabelle NICHT vorhanden (keine Ergebnisse)
        await expect(page.locator('[data-testid="results-table"]')).not.toBeVisible();
    });

    test('Submit des leeren Suchformulars zeigt Validierungsfehler-Banner', async ({ page }) => {
        // Arrange: Suchseite öffnen
        await page.goto('/profilesearch/search');

        // Act: Formular ohne Eingaben absenden
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search/);

        // Assert: Validierungsfehler-Banner vorhanden
        await expect(page.locator('[data-testid="validation-error-banner"]')).toBeVisible();

        // Assert: Ergebnistabelle nicht sichtbar
        await expect(page.locator('[data-testid="results-table"]')).not.toBeVisible();
    });

    // =========================================================================
    // Szenario 3: Suche mit Suchbegriff
    // =========================================================================

    test('Suche mit Suchbegriff "Mock" liefert Ergebnistabelle mit Treffern', async ({ page }) => {
        // Arrange: Suchseite öffnen
        await page.goto('/profilesearch/search');

        // Act: Suchbegriff eingeben und Suchen klicken
        await page.locator('[data-testid="input-search-term"]').fill('Mock');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search\?.*searchTerm=Mock/);

        // Assert: Ergebnistabelle erscheint
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Assert: Mindestens eine Zeile vorhanden (50 Mock-Freelancer passen auf "Mock")
        const rows = page.locator('[data-testid="results-table"] tbody tr');
        await expect(rows.first()).toBeVisible();
    });

    test('Suche über URL-Parameter zeigt Ergebnistabelle', async ({ page }) => {
        // Direkter Aufruf mit searchTerm-Parameter
        await page.goto('/profilesearch/search?searchTerm=Mock');

        // Assert: Ergebnistabelle vorhanden und Kein Validierungsfehler
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
        await expect(page.locator('[data-testid="validation-error-banner"]')).not.toBeVisible();
    });

    test('Suche zeigt Treffer-Anzahl', async ({ page }) => {
        await page.goto('/profilesearch/search?searchTerm=Mock');

        // Der Controller gibt ${totalCount} + " Treffer" aus
        await expect(page.locator('text=/\\d+ Treffer/')).toBeVisible();
    });

    // =========================================================================
    // Szenario 4: Suche mit Tagessatz-Filter
    // =========================================================================

    test('Tagessatz-Filter salaryPerDayFrom=800 liefert nur Freelancer mit Tagessatz >= 800', async ({ page }) => {
        // Mock-Daten: Tagessatz = 400 + i*10; bei i=40 → 800 €, i=49 → 890 €
        // Erwartet: Genau 10 Treffer (i=40..49)
        await page.goto('/profilesearch/search?salaryPerDayFrom=800');

        // Assert: Ergebnistabelle vorhanden
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Assert: Exakt 10 Treffer
        const rows = page.locator('[data-testid="results-table"] tbody tr');
        await expect(rows).toHaveCount(10);
    });

    test('Tagessatz-Filter salaryPerDayFrom und salaryPerDayTo einschränken Ergebnisse', async ({ page }) => {
        // Mock-Daten: Tagessatz = 400 + i*10
        // salaryPerDayFrom=500: i >= 10 → 40 Treffer
        // salaryPerDayTo=600:   i <= 20 → 11 Treffer (500, 510, ..., 600 = i=10..20)
        await page.goto('/profilesearch/search?salaryPerDayFrom=500&salaryPerDayTo=600');

        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        const rows = page.locator('[data-testid="results-table"] tbody tr');
        await expect(rows).toHaveCount(11);
    });

    test('Tagessatz-Felder werden per Formular übergeben', async ({ page }) => {
        await page.goto('/profilesearch/search');

        // Tagessatz von eingeben
        await page.locator('[data-testid="input-salary-from"]').fill('800');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search\?.*salaryPerDayFrom=800/);

        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Formular-Feld zeigt eingegebenen Wert wieder an
        await expect(page.locator('[data-testid="input-salary-from"]')).toHaveValue('800');
    });

    // =========================================================================
    // Szenario 5: Kontaktsperre-Markierung
    // =========================================================================

    test('Kontaktgesperrte Freelancer haben die Klasse row-forbidden in der Ergebnistabelle', async ({ page }) => {
        // Mock-Daten: i % 7 == 0 → contactForbidden; bei searchTerm=Mock (alle 50)
        // Verbotene Zeilen: i=0,7,14,21,28,35,42 → 7 Stück (alle in ersten 20 Ergebnissen)
        await page.goto('/profilesearch/search?searchTerm=Mock');

        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // row-forbidden Zeilen existieren (min. 3 davon in den ersten 20 Ergebnissen: i=0,7,14)
        const forbiddenRows = page.locator('[data-testid="results-table"] tbody tr.row-forbidden');
        await expect(forbiddenRows.first()).toBeVisible();

        // Anzahl der verbotenen Zeilen auf der ersten Seite (20 Ergebnisse, 3 verbotene: i=0,7,14)
        await expect(forbiddenRows).toHaveCount(3);
    });

    // =========================================================================
    // Szenario 6: Sortierung
    // =========================================================================

    test('Klick auf "Name 1"-Spaltenheader ändert URL auf sortField=name1', async ({ page }) => {
        // Suchseite mit Ergebnissen laden
        await page.goto('/profilesearch/search?searchTerm=Mock');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Auf "Name 1" Spaltenheader-Link klicken
        const name1Header = page.locator('[data-testid="results-table"] thead th').first().locator('a');
        await name1Header.click();
        await page.waitForURL(/sortField=name1/);

        // URL enthält sortField=name1
        await expect(page).toHaveURL(/sortField=name1/);
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();
    });

    test('Zweimaliger Klick auf "Name 1" wechselt Sortierrichtung zu desc', async ({ page }) => {
        // Erster Klick: sortField=name1&sortDir=asc (Standard)
        await page.goto('/profilesearch/search?searchTerm=Mock&sortField=name1&sortDir=asc');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Auf "Name 1" Spaltenheader klicken → wechselt zu desc
        const name1Header = page.locator('[data-testid="results-table"] thead th').first().locator('a');
        await name1Header.click();
        await page.waitForURL(/sortDir=desc/);

        await expect(page).toHaveURL(/sortField=name1/);
        await expect(page).toHaveURL(/sortDir=desc/);
    });

    test('Zurücksetzen-Button löscht Suchkriterien und zeigt leere Suchseite', async ({ page }) => {
        // Suchseite mit Kriterien laden
        await page.goto('/profilesearch/search?searchTerm=Mock');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Zurücksetzen klicken
        await page.locator('[data-testid="btn-reset"]').click();
        await page.waitForURL(/\/profilesearch\/search$/);

        // URL enthält keine Parameter mehr
        await expect(page).toHaveURL(/\/profilesearch\/search$/);

        // Validierungsfehler erscheint (leere Suche)
        await expect(page.locator('[data-testid="validation-error-banner"]')).toBeVisible();
    });

    // =========================================================================
    // Szenario 7: Zeile anklicken öffnet Freiberufler mit returnTo-Parameter
    // =========================================================================

    test('Klick auf Ergebnis-Zeile öffnet Freiberufler-Formular mit returnTo=profilesearch-search', async ({ page }) => {
        // Suchseite mit Ergebnissen laden
        await page.goto('/profilesearch/search?searchTerm=Mock');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Erste Zeile anklicken (Mock Freelancer 0, ID 100)
        const firstRow = page.locator('[data-testid="results-table"] tbody tr').first();
        await firstRow.click();

        // Warten auf Freelancer-Formular mit returnTo-Parameter
        await page.waitForURL(/\/freelancer\/\d+\?returnTo=profilesearch-search/);

        // URL enthält returnTo=profilesearch-search
        await expect(page).toHaveURL(/returnTo=profilesearch-search/);
    });

});
