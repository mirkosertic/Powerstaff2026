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
    // Szenario 5: Kontaktsperre-Markierung
    // =========================================================================

    test('Kontaktgesperrte Freelancer haben die Klasse row-forbidden in der Ergebnistabelle', async ({ page }) => {
        // Seed-Daten (V100__e2e_seed.sql): 4 Freelancer, davon 1 mit contactforbidden=TRUE (ID 1004, E2E-004)
        await page.goto('/profilesearch/search?searchTerm=Mock');

        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Freelancer 1004 hat contactforbidden=TRUE → Hauptzeile trägt row-forbidden
        // (SERP-Zeilen desselben Freelancers können ebenfalls row-forbidden haben)
        await expect(page.locator('[data-testid="result-row-1004"]')).toHaveClass(/row-forbidden/);
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

    test('Klick auf Ergebnis-Zeile öffnet Freiberufler-Formular mit returnTo zur Suchergebnisseite', async ({ page }) => {
        // Suchseite mit Ergebnissen laden
        await page.goto('/profilesearch/search?searchTerm=Mock');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Erste Zeile anklicken (nicht-SERP-Zeile)
        const firstRow = page.locator('[data-testid="results-table"] tbody tr:not(.serp-row)').first();
        await firstRow.click();

        // Warten auf Freelancer-Formular – returnTo enthält die kodierte Profilsuche-URL
        await page.waitForURL(/\/freelancer\/\d+\?returnTo=%2Fprofilesearch/);

        // URL enthält returnTo mit der profilesearch-Suchadresse
        await expect(page).toHaveURL(/returnTo=%2Fprofilesearch%2Fsearch/);

        // Zurück-Button zur Profilsuche ist sichtbar
        await expect(page.locator('a:has-text("Zur Profilsuche")')).toBeVisible();
    });

    // =========================================================================
    // Szenario 8: Tag-Chip-Selektion im Suchformular
    // =========================================================================

    test('Tag selektieren: Chip erhält Klasse "selected" und ID erscheint im hidden input', async ({ page }) => {
        // Arrange: Suchseite laden – allTags wird immer befüllt, auch ohne Kriterien
        await page.goto('/profilesearch/search');
        const chip1 = page.locator('[data-testid="tag-chip-1"]');
        await expect(chip1).toBeVisible();

        // Vorbedingung: Chip noch nicht selektiert
        await expect(chip1).not.toHaveClass(/selected/);
        await expect(page.locator('#tag-ids-input')).toHaveValue('');

        // Act: Tag-Chip klicken
        await chip1.click();

        // Assert: Chip selektiert, ID im hidden input
        await expect(chip1).toHaveClass(/selected/);
        await expect(page.locator('#tag-ids-input')).toHaveValue('1');
    });

    test('Tag deselektieren: Chip verliert "selected", ID wird aus hidden input entfernt', async ({ page }) => {
        // Arrange: Suchseite mit vorselektiertem Tag (tagIds=1 ≙ Java)
        await page.goto('/profilesearch/search?tagIds=1');
        const chip1 = page.locator('[data-testid="tag-chip-1"]');
        await expect(chip1).toBeVisible();

        // Vorbedingung: Chip ist bereits selektiert (Thymeleaf-Rendering)
        await expect(chip1).toHaveClass(/selected/);
        await expect(page.locator('#tag-ids-input')).toHaveValue('1');

        // Act: nochmals klicken → deselektieren
        await chip1.click();

        // Assert: Chip nicht mehr selektiert, Input leer
        await expect(chip1).not.toHaveClass(/selected/);
        await expect(page.locator('#tag-ids-input')).toHaveValue('');
    });

    test('Mehrere Tags selektieren: beide IDs kommasepariert im hidden input', async ({ page }) => {
        // Arrange: leere Suchseite
        await page.goto('/profilesearch/search');
        const chip1 = page.locator('[data-testid="tag-chip-1"]');
        const chip2 = page.locator('[data-testid="tag-chip-2"]');
        await expect(chip1).toBeVisible();
        await expect(chip2).toBeVisible();

        // Act: beide Tags selektieren
        await chip1.click();
        await chip2.click();

        // Assert: beide selektiert
        await expect(chip1).toHaveClass(/selected/);
        await expect(chip2).toHaveClass(/selected/);

        // Assert: hidden input enthält beide IDs
        const value = await page.locator('#tag-ids-input').inputValue();
        expect(value.split(',')).toContain('1');
        expect(value.split(',')).toContain('2');
    });

    test('Regression Substring-Bug: tagIds=1 selektiert nur Tag-ID 1, nicht Tag-ID 10 oder 11', async ({ page }) => {
        // Reproduziert den Fix: früher nutzte Thymeleaf #strings.contains("1,10,11", "1"),
        // was für Tags mit ID 10 und 11 ebenfalls true lieferte.
        // Seed: E2E-Tag-10 hat ID 10, E2E-Tag-11 hat ID 11 (explizite IDs in V100__e2e_seed.sql).
        await page.goto('/profilesearch/search?tagIds=1');

        const chip1  = page.locator('[data-testid="tag-chip-1"]');
        const chip10 = page.locator('[data-testid="tag-chip-10"]');
        const chip11 = page.locator('[data-testid="tag-chip-11"]');

        await expect(chip1).toBeVisible();
        await expect(chip10).toBeVisible();
        await expect(chip11).toBeVisible();

        // Nur ID 1 ist selektiert
        await expect(chip1).toHaveClass(/selected/);

        // ID 10 und 11 dürfen NICHT selektiert sein (kein Substring-Match)
        await expect(chip10).not.toHaveClass(/selected/);
        await expect(chip11).not.toHaveClass(/selected/);

        // Auch der hidden input darf nur "1" enthalten
        await expect(page.locator('#tag-ids-input')).toHaveValue('1');
    });

    test('Regression: nach Suche mit tagIds=1 bleiben nur Tag-ID 1 selektiert', async ({ page }) => {
        // Suche ausführen → Server rendert Seite erneut → Thymeleaf-Selektion wird geprüft
        await page.goto('/profilesearch/search?tagIds=1');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search\?.*tagIds=1/);

        const chip1  = page.locator('[data-testid="tag-chip-1"]');
        const chip10 = page.locator('[data-testid="tag-chip-10"]');
        const chip11 = page.locator('[data-testid="tag-chip-11"]');

        await expect(chip1).toHaveClass(/selected/);
        await expect(chip10).not.toHaveClass(/selected/);
        await expect(chip11).not.toHaveClass(/selected/);
    });

    // =========================================================================
    // Szenario 9: Tag-Navigation – Profilsuche → Tag-Klick → Freiberufler-Liste → Zurück
    // =========================================================================

    test('Tag in Suchergebnis klicken navigiert zu Freiberufler-Suche mit tagId-Filter und zurück zur Profilsuche', async ({ page }) => {
        // Suchseite mit Ergebnissen laden – Freelancer 1001 (Mustermann) hat Tags "Java" + "München" (siehe V100__e2e_seed.sql)
        await page.goto('/profilesearch/search?searchTerm=Mustermann');
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Zeile für Freelancer 1001 finden
        const row1001 = page.locator('[data-testid="result-row-1001"]');
        await expect(row1001).toBeVisible();

        // Tag "Java" in der Ergebniszeile klicken (Tag-Chips haben data-testid="result-tag-{id}")
        // Wir wissen nicht die exakte Tag-ID, also suchen wir nach dem Text "Java"
        const javaTagChip = row1001.locator('.chip.chip-xs').filter({ hasText: 'Java' });
        await expect(javaTagChip).toBeVisible();

        // Tag-ID aus dem data-testid extrahieren
        const tagTestId = await javaTagChip.getAttribute('data-testid');
        const tagId = tagTestId?.match(/result-tag-(\d+)/)?.[1];
        expect(tagId).toBeTruthy();

        // Auf Tag-Chip klicken → navigiert zu /freelancer/search?tagId=...
        await javaTagChip.click();

        // URL sollte /freelancer/search?tagId=X sein (returnTo wird vom JS im Template hinzugefügt)
        await page.waitForURL(/\/freelancer\/search\?tagId=\d+/, { timeout: 10_000 });
        await expect(page).toHaveURL(new RegExp(`tagId=${tagId}`));

        // Ergebnistabelle auf Freelancer-Suchseite ist vorhanden
        await expect(page.locator('[data-testid="results-table"]')).toBeVisible();

        // Mindestens ein Ergebnis vorhanden (Freelancer 1001 mit Tag "Java")
        const resultRows = page.locator('[data-testid="results-table"] tbody tr');
        await expect(resultRows.first()).toBeVisible();
        const rowCount = await resultRows.count();
        expect(rowCount).toBeGreaterThan(0);

        // Prüfen, dass Freelancer 1001 in den Ergebnissen ist
        await expect(page.locator('[data-testid="freelancer-row-1001"]')).toBeVisible();

        // "Zur Profilsuche"-Button ist sichtbar
        const backButton = page.locator('a:has-text("Zur Profilsuche")');
        await expect(backButton).toBeVisible();

        // Zurück zur Profilsuche klicken
        await backButton.click();
        await page.waitForURL(/\/profilesearch\/search/, { timeout: 10_000 });

        // Wir sind wieder auf der Profilsuche-Seite
        await expect(page).toHaveURL(/\/profilesearch\/search/);
        await expect(page.locator('[data-testid="tab-search"]')).toBeVisible();
    });

});

