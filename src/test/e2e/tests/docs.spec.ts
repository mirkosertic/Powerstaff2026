import { test, expect } from '@playwright/test';
import * as path from 'path';

const SCREENSHOTS = path.resolve(__dirname, '../../../../docs/screenshots');
const COMPONENTS = path.resolve(__dirname, '../../../../docs/screenshots/components');

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
        await page.goto('/profilesearch');
        await page.waitForURL(/\/profilesearch\/chat\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `${SCREENSHOTS}/profilsuche-ki-chat.png`, fullPage: true });
    });

    test('Profilsuche – Klassische Suche Suchmaske', async ({ page }) => {
        await page.goto('/profilesearch/search', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/profilsuche-klassisch-suchmaske.png`, fullPage: true });
    });

    test('Profilsuche – Klassische Suche Ergebnisse', async ({ page }) => {
        // Suchergebnisse werden auf /profilesearch/search mit Query-Parametern angezeigt (kein separates URL-Segment)
        await page.goto('/profilesearch/search', { waitUntil: 'networkidle' });
        await page.locator('[data-testid="input-search-term"]').fill('Mock');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/profilesearch\/search\?.*searchTerm=Mock/);
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
        await page.goto('/admin/benutzer', { waitUntil: 'networkidle' });
        await page.screenshot({ path: `${SCREENSHOTS}/admin-benutzer.png`, fullPage: true });
    });

});

// ===========================================================================
// Komponenten-Screenshots
// ===========================================================================

test.describe('Komponenten-Screenshots', () => {

    // FL 20001 (Ignaz Flausenmann) – vollständig ausgefüllter Datensatz mit Kontakten + Historie + Tags
    async function openFL20001(page: any): Promise<void> {
        await page.goto('/freelancer/20001', { waitUntil: 'networkidle' });
    }

    // FL 20001 mit gemerktem Projekt – zuerst Projekt 40001 besuchen (setzt remembered_project in DB)
    async function openFL20001WithProject(page: any): Promise<void> {
        await page.goto('/project/40001', { waitUntil: 'networkidle' });
        await page.goto('/freelancer/20001', { waitUntil: 'networkidle' });
    }

    // --- A: App-Navigation & Toolbar ---

    test('App-Navigation', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#app-nav').screenshot({ path: `${COMPONENTS}/app-navigation.png` });
    });

    test('Toolbar – Gesamt', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#toolbar').screenshot({ path: `${COMPONENTS}/toolbar-gesamt.png` });
    });

    test('Toolbar – Datensatz-Navigation', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#tb-nav').screenshot({ path: `${COMPONENTS}/toolbar-navigation.png` });
    });

    test('Toolbar – Audit-Info', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#tb-audit').screenshot({ path: `${COMPONENTS}/toolbar-audit.png` });
    });

    test('Toolbar – Aktionen', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#tb-actions').screenshot({ path: `${COMPONENTS}/toolbar-aktionen.png` });
    });

    test('Toolbar – Gemerktes Projekt', async ({ page }) => {
        await openFL20001WithProject(page);
        await page.locator('#toolbar').screenshot({ path: `${COMPONENTS}/toolbar-gemerktes-projekt.png` });
    });

    // --- B: Formular-Karten ---

    test('Formular-Karte – Adresse', async ({ page }) => {
        await openFL20001(page);
        await page.locator('.fcard:has(#fcard-adresse)').screenshot({ path: `${COMPONENTS}/fcard-adresse.png` });
    });

    test('Formular-Karte – Kontaktinformationen', async ({ page }) => {
        await openFL20001(page);
        await page.locator('.fcard:has(#fcard-kontakt)').screenshot({ path: `${COMPONENTS}/fcard-kontaktinformationen.png` });
    });

    test('Formular-Karte – Verfügbarkeit & Konditionen', async ({ page }) => {
        await openFL20001(page);
        await page.locator('.fcard:has(#fcard-verfuegbarkeit)').screenshot({ path: `${COMPONENTS}/fcard-verfuegbarkeit.png` });
    });

    test('Formular-Karte – Kodierung & Skills', async ({ page }) => {
        await openFL20001(page);
        await page.locator('.fcard:has(#fcard-kodierung)').screenshot({ path: `${COMPONENTS}/fcard-kodierung.png` });
    });

    test('Formular-Karte – Kontaktmöglichkeiten', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#contacts-card').screenshot({ path: `${COMPONENTS}/fcard-kontaktmoeglichkeiten.png` });
    });

    test('Formular-Karte – Kontakthistorie', async ({ page }) => {
        await openFL20001(page);
        await page.locator('#history-card').screenshot({ path: `${COMPONENTS}/fcard-kontakthistorie.png` });
    });

    // --- C: Einzelne Listeneinträge ---

    test('Kontakt-Eintrag (einzeln)', async ({ page }) => {
        await openFL20001(page);
        await expect(page.locator('#contacts-list .citem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#contacts-list .citem').first().screenshot({ path: `${COMPONENTS}/contact-item.png` });
    });

    test('Historieneintrag (einzeln)', async ({ page }) => {
        await openFL20001(page);
        await expect(page.locator('#history-list .hitem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#history-list .hitem').first().screenshot({ path: `${COMPONENTS}/history-item.png` });
    });

    // --- D: Modale Dialoge ---

    test('Modal – Kontakt hinzufügen', async ({ page }) => {
        await openFL20001(page);
        await page.locator('[data-testid="btn-add-contact"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/modal-kontakt-hinzufuegen.png` });
    });

    test('Modal – Historieneintrag anlegen', async ({ page }) => {
        await openFL20001(page);
        await page.locator('[data-testid="btn-add-history"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/modal-historieneintrag.png` });
    });

    test('Modal – Löschen-Bestätigung', async ({ page }) => {
        await openFL20001(page);
        await page.locator('[data-testid="btn-delete"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/modal-loeschen.png` });
    });

    // --- E: Banner & Status ---

    test('Banner – Kontaktsperre', async ({ page }) => {
        // FL 20006 (Petra Nüsselein) hat contactForbidden=TRUE im Dev-Seed
        await page.goto('/freelancer/20006', { waitUntil: 'networkidle' });
        await page.locator('.banner-forbidden').screenshot({ path: `${COMPONENTS}/banner-kontaktsperre.png` });
    });

    test('Banner – Erfolgreich gespeichert', async ({ page }) => {
        await page.goto('/freelancer/20001?saved=true', { waitUntil: 'networkidle' });
        await expect(page.locator('#banner-success')).toBeVisible({ timeout: 5_000 });
        await page.locator('#banner-success').screenshot({ path: `${COMPONENTS}/banner-success.png` });
    });

    // --- F: Suchmasken-Detail ---

    test('Suchergebnisse – Tabellenzeile', async ({ page }) => {
        await page.goto('/freelancer/new');
        await page.locator('[data-testid="btn-search"]').click();
        await page.waitForURL(/\/freelancer\/search/);
        await expect(page.locator('[data-testid="results-table"] tbody tr').first()).toBeVisible({ timeout: 10_000 });
        await page.locator('[data-testid="results-table"] tbody tr').first().screenshot({ path: `${COMPONENTS}/search-results-row.png` });
    });

    test('Profilsuche – Chat-Eingabebereich', async ({ page }) => {
        await page.goto('/profilesearch');
        await page.waitForURL(/\/profilesearch\/chat\/\d+/);
        await page.waitForLoadState('networkidle');
        await page.locator('#chat-input-area').screenshot({ path: `${COMPONENTS}/profilsuche-chat-eingabe.png` });
    });

});

// ===========================================================================
// Partner-Komponenten-Screenshots
// ===========================================================================

test.describe('Partner-Komponenten-Screenshots', () => {

    // Partner 10001 – Codewald GmbH: hat Kontakte + Historieneintrag im Dev-Seed
    async function openPartner10001(page: any): Promise<void> {
        await page.goto('/partner/10001', { waitUntil: 'networkidle' });
    }

    test('Partner – Formular-Karte Adresse', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('.fcard:has(#fcard-adresse)').screenshot({ path: `${COMPONENTS}/partner-fcard-adresse.png` });
    });

    test('Partner – Formular-Karte Konditionen', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('.fcard:has(#fcard-konditionen)').screenshot({ path: `${COMPONENTS}/partner-fcard-konditionen.png` });
    });

    test('Partner – Formular-Karte Kontaktmöglichkeiten', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('#contacts-card').screenshot({ path: `${COMPONENTS}/partner-fcard-kontaktmoeglichkeiten.png` });
    });

    test('Partner – Kontakt-Eintrag (einzeln)', async ({ page }) => {
        await openPartner10001(page);
        await expect(page.locator('#contacts-list .citem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#contacts-list .citem').first().screenshot({ path: `${COMPONENTS}/partner-contact-item.png` });
    });

    test('Partner – Formular-Karte Zugeordnete Freiberufler', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('#freelancers-card').screenshot({ path: `${COMPONENTS}/partner-fcard-freiberufler.png` });
    });

    test('Partner – Formular-Karte Kontakthistorie', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('#history-card').screenshot({ path: `${COMPONENTS}/partner-fcard-kontakthistorie.png` });
    });

    test('Partner – Historieneintrag (einzeln)', async ({ page }) => {
        await openPartner10001(page);
        await expect(page.locator('#history-list .hitem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#history-list .hitem').first().screenshot({ path: `${COMPONENTS}/partner-history-item.png` });
    });

    test('Partner – Banner Kontaktsperre', async ({ page }) => {
        // Partner 10007 (Bitsalat AG) hat contactForbidden=TRUE im Dev-Seed
        await page.goto('/partner/10007', { waitUntil: 'networkidle' });
        await page.locator('.banner-forbidden').screenshot({ path: `${COMPONENTS}/partner-banner-kontaktsperre.png` });
    });

    test('Partner – Modal Kontakt hinzufügen', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('[data-testid="btn-add-contact"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/partner-modal-kontakt-hinzufuegen.png` });
    });

    test('Partner – Modal Historieneintrag', async ({ page }) => {
        await openPartner10001(page);
        await page.locator('[data-testid="btn-add-history"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/partner-modal-historieneintrag.png` });
    });

});

// ===========================================================================
// Kunden-Komponenten-Screenshots
// ===========================================================================

test.describe('Kunden-Komponenten-Screenshots', () => {

    // Kunde 30001 – Mondfahrt Automobil: hat Kontakte + Historieneintrag im Dev-Seed
    async function openKunde30001(page: any): Promise<void> {
        await page.goto('/kunde/30001', { waitUntil: 'networkidle' });
    }

    test('Kunden – Formular-Karte Adresse', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('.fcard:has(#fcard-adresse)').screenshot({ path: `${COMPONENTS}/kunden-fcard-adresse.png` });
    });

    test('Kunden – Formular-Karte Kontaktinformationen', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('.fcard:has(#fcard-kontakt)').screenshot({ path: `${COMPONENTS}/kunden-fcard-kontaktinformationen.png` });
    });

    test('Kunden – Formular-Karte Konditionen', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('.fcard:has(#fcard-konditionen)').screenshot({ path: `${COMPONENTS}/kunden-fcard-konditionen.png` });
    });

    test('Kunden – Formular-Karte Projekte', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('.fcard:has(#fcard-projekte)').screenshot({ path: `${COMPONENTS}/kunden-fcard-projekte.png` });
    });

    test('Kunden – Formular-Karte Kontaktmöglichkeiten', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('#contacts-card').screenshot({ path: `${COMPONENTS}/kunden-fcard-kontaktmoeglichkeiten.png` });
    });

    test('Kunden – Kontakt-Eintrag (einzeln)', async ({ page }) => {
        await openKunde30001(page);
        await expect(page.locator('#contacts-list .citem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#contacts-list .citem').first().screenshot({ path: `${COMPONENTS}/kunden-contact-item.png` });
    });

    test('Kunden – Formular-Karte Kontakthistorie', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('#history-card').screenshot({ path: `${COMPONENTS}/kunden-fcard-kontakthistorie.png` });
    });

    test('Kunden – Historieneintrag (einzeln)', async ({ page }) => {
        await openKunde30001(page);
        await expect(page.locator('#history-list .hitem').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#history-list .hitem').first().screenshot({ path: `${COMPONENTS}/kunden-history-item.png` });
    });

    test('Kunden – Banner Kontaktsperre', async ({ page }) => {
        // Kunde 30008 (Modebombe SE) hat contactForbidden=TRUE im Dev-Seed
        await page.goto('/kunde/30008', { waitUntil: 'networkidle' });
        await page.locator('.banner-forbidden').screenshot({ path: `${COMPONENTS}/kunden-banner-kontaktsperre.png` });
    });

    test('Kunden – Modal Kontakt hinzufügen', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('[data-testid="btn-add-contact"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/kunden-modal-kontakt-hinzufuegen.png` });
    });

    test('Kunden – Modal Historieneintrag', async ({ page }) => {
        await openKunde30001(page);
        await page.locator('[data-testid="btn-add-history"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/kunden-modal-historieneintrag.png` });
    });

});

// ===========================================================================
// Projekt-Komponenten-Screenshots
// ===========================================================================

test.describe('Projekt-Komponenten-Screenshots', () => {

    // Projekt 40001 – PRJ-2026-001: hat Positionen, Kunde 30001, im Dev-Seed
    async function openProjekt40001(page: any): Promise<void> {
        await page.goto('/project/40001', { waitUntil: 'networkidle' });
    }

    test('Projekt – Formular-Karte Allgemein', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('.fcard:has(#fcard-allgemein)').screenshot({ path: `${COMPONENTS}/projekt-fcard-allgemein.png` });
    });

    test('Projekt – Formular-Karte Beschreibung', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('.fcard:has(#fcard-beschreibung)').screenshot({ path: `${COMPONENTS}/projekt-fcard-beschreibung.png` });
    });

    test('Projekt – Formular-Karte Einsatz', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('.fcard:has(#fcard-einsatz)').screenshot({ path: `${COMPONENTS}/projekt-fcard-einsatz.png` });
    });

    test('Projekt – Formular-Karte Konditionen', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('.fcard:has(#fcard-konditionen)').screenshot({ path: `${COMPONENTS}/projekt-fcard-konditionen.png` });
    });

    test('Projekt – Formular-Karte Zuordnung', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('.fcard:has(#fcard-zuordnung)').screenshot({ path: `${COMPONENTS}/projekt-fcard-zuordnung.png` });
    });

    test('Projekt – Formular-Karte Positionen', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('#positions-card').screenshot({ path: `${COMPONENTS}/projekt-fcard-positionen.png` });
    });

    test('Projekt – Positions-Zeile (einzeln)', async ({ page }) => {
        await openProjekt40001(page);
        await expect(page.locator('#fcard-positions tbody tr').first()).toBeVisible({ timeout: 5_000 });
        await page.locator('#fcard-positions tbody tr').first().screenshot({ path: `${COMPONENTS}/projekt-position-row.png` });
    });

    test('Projekt – Formular-Karte Projekthistorie', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('#history-card').screenshot({ path: `${COMPONENTS}/projekt-fcard-historie.png` });
    });

    test('Projekt – Modal Historieneintrag', async ({ page }) => {
        await openProjekt40001(page);
        await page.locator('[data-testid="btn-add-history"]').click();
        await expect(page.locator('.mbk:not(.hidden) .mbox')).toBeVisible({ timeout: 5_000 });
        await page.locator('.mbk:not(.hidden) .mbox').screenshot({ path: `${COMPONENTS}/projekt-modal-historieneintrag.png` });
    });

});
