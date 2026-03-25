import { test, expect } from '@playwright/test';

test.describe('Administration', () => {

    test('Historientypen-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/historientypen');

        await expect(page.locator('[data-testid^="histtype-row-"]').first()).toBeVisible();
    });

    test('Neuen Historientyp anlegen', async ({ page }) => {
        await page.goto('/admin/historientypen');

        await page.locator('[data-testid="btn-new-histtype"]').click();
        await page.locator('#new-ht-description').fill('E2E-Testtyp');
        await page.locator('[data-testid="btn-submit-histtype"]').click();
        await page.waitForURL(/\/admin\/historientypen/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Testtyp$/ }).first()).toBeVisible();
    });

    test('Historientyp bearbeiten: Edit-Modal öffnen und Bezeichnung ändern', async ({ page }) => {
        // Voraussetzung: Mindestens ein Historientyp vorhanden (Seed-Daten)
        await page.goto('/admin/historientypen');

        // Ersten Edit-Button klicken
        const editBtn = page.locator('[data-testid^="btn-edit-histtype-"]').first();
        await editBtn.click();
        await expect(page.locator('#modal-edit-ht')).not.toHaveClass(/hidden/);

        // Bezeichnung überschreiben
        await page.locator('#edit-ht-description').fill('E2E-Bearbeiteter-Typ');
        await page.locator('#modal-edit-ht button[type="submit"]').click();
        await page.waitForURL(/\/admin\/historientypen/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Bearbeiteter-Typ$/ }).first()).toBeVisible();
    });

    test('Historientyp löschen: Löschen-Button → Bestätigung → Zeile entfernt', async ({ page }) => {
        // Eigenen Historientyp anlegen, den wir dann löschen
        await page.goto('/admin/historientypen');

        await page.locator('[data-testid="btn-new-histtype"]').click();
        await page.locator('#new-ht-description').fill('E2E-Loeschtyp');
        await page.locator('[data-testid="btn-submit-histtype"]').click();
        await page.waitForURL(/\/admin\/historientypen/);

        // Lösch-Button → HTML-Modal öffnet sich
        const deleteBtn = page.locator('td').filter({ hasText: /^E2E-Loeschtyp$/ })
            .locator('..') // zur Zeile
            .locator('[data-testid^="btn-delete-histtype-"]');

        await deleteBtn.click();
        await expect(page.locator('#modal-delete-ht')).toBeVisible();
        await page.locator('[data-testid="btn-confirm-delete-histtype"]').click();
        await page.waitForURL(/\/admin\/historientypen/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Loeschtyp$/ })).not.toBeAttached();
    });

    test('Positionsstatus-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await expect(page.locator('[data-testid^="posstatus-row-"]').first()).toBeVisible();
    });

    test('Neuen Positionsstatus anlegen', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        await page.locator('[data-testid="btn-new-posstatus"]').click();
        await page.locator('#new-status-description').fill('E2E-Status');
        await page.locator('#new-status-color').fill('#e0f2fe');
        await page.locator('#new-status-colortext').fill('#0369a1');
        await page.locator('[data-testid="btn-submit-posstatus"]').click();
        await page.waitForURL(/\/admin\/positionsstatus/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Status$/ }).first()).toBeVisible();
    });

    test('Positionsstatus bearbeiten: Edit-Modal öffnen und Bezeichnung ändern', async ({ page }) => {
        await page.goto('/admin/positionsstatus');

        // Ersten Edit-Button klicken
        const editBtn = page.locator('[data-testid^="btn-edit-posstatus-"]').first();
        await editBtn.click();
        await expect(page.locator('#modal-edit-status')).not.toHaveClass(/hidden/);

        // Bezeichnung ändern
        await page.locator('#edit-status-description').fill('E2E-Bearbeiteter-Status');
        await page.locator('#modal-edit-status button[type="submit"]').click();
        await page.waitForURL(/\/admin\/positionsstatus/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Bearbeiteter-Status$/ }).first()).toBeVisible();
    });

    test('Positionsstatus löschen: Löschen-Button → Bestätigung → Zeile entfernt', async ({ page }) => {
        // Eigenen Status anlegen, den wir dann löschen
        await page.goto('/admin/positionsstatus');

        await page.locator('[data-testid="btn-new-posstatus"]').click();
        await page.locator('#new-status-description').fill('E2E-Loeschstatus');
        await page.locator('#new-status-color').fill('#fef3c7');
        await page.locator('#new-status-colortext').fill('#92400e');
        await page.locator('[data-testid="btn-submit-posstatus"]').click();
        await page.waitForURL(/\/admin\/positionsstatus/);

        // Lösch-Button → HTML-Modal öffnet sich
        const deleteBtn = page.locator('td').filter({ hasText: /^E2E-Loeschstatus$/ })
            .locator('..') // zur Zeile
            .locator('[data-testid^="btn-delete-posstatus-"]');

        await deleteBtn.click();
        await expect(page.locator('#modal-delete-status')).toBeVisible();
        await page.locator('[data-testid="btn-confirm-delete-posstatus"]').click();
        await page.waitForURL(/\/admin\/positionsstatus/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Loeschstatus$/ })).not.toBeAttached();
    });

    test('Tags-Seite lädt und zeigt Einträge', async ({ page }) => {
        await page.goto('/admin/tags');

        await expect(page.locator('[data-testid^="tag-row-"]').first()).toBeVisible();
    });

    test('Neuen Tag anlegen', async ({ page }) => {
        await page.goto('/admin/tags');

        await page.locator('#new-tag-type').selectOption('SCHWERPUNKT');
        await page.locator('#new-tag-name').fill('E2E-Tag-Playwright');
        await page.locator('[data-testid="btn-submit-tag"]').click();
        await page.waitForURL(/\/admin\/tags/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Tag-Playwright$/ }).first()).toBeVisible();
    });

    test('Tag bearbeiten: Edit-Modal öffnen und Namen ändern', async ({ page }) => {
        await page.goto('/admin/tags');

        // Ersten Edit-Button klicken
        const editBtn = page.locator('[data-action="edit-tag"]').first();
        await editBtn.click();
        await expect(page.locator('#modal-edit-tag')).not.toHaveClass(/hidden/);

        // Namen überschreiben
        await page.locator('#edit-tag-name').fill('E2E-Bearbeiteter-Tag');
        await page.locator('#modal-edit-tag button[type="submit"]').click();
        await page.waitForURL(/\/admin\/tags/);

        await expect(page.locator('td').filter({ hasText: /^E2E-Bearbeiteter-Tag$/ }).first()).toBeVisible();
    });

    test('Tag löschen: Löschen-Button → HTML-Modal bestätigen → Zeile entfernt', async ({ page }) => {
        // Tag anlegen, der gelöscht werden soll
        await page.goto('/admin/tags');

        await page.locator('#new-tag-type').selectOption('BEMERKUNG');
        await page.locator('#new-tag-name').fill('E2E-Loeschtag');
        await page.locator('[data-testid="btn-submit-tag"]').click();
        await page.waitForURL(/\/admin\/tags/);

        // Tag-Zeile finden und Lösch-Button klicken
        const tagRow = page.locator('td').filter({ hasText: /^E2E-Loeschtag$/ }).locator('..');
        const deleteBtn = tagRow.locator('[data-action="delete-tag"]');

        await deleteBtn.click();
        await expect(page.locator('#modal-delete-tag')).toBeVisible();
        await page.locator('[data-testid="btn-confirm-delete-tag"]').click();

        // Zeile soll nach AJAX-Löschen aus dem DOM verschwinden
        await expect(page.locator('td').filter({ hasText: /^E2E-Loeschtag$/ })).not.toBeAttached({
            timeout: 5_000,
        });
    });

    // -------------------------------------------------------------------------
    // Benutzerverwaltung
    // -------------------------------------------------------------------------

    test('Benutzerverwaltung-Seite lädt und zeigt Tabelle mit Benutzern', async ({ page }) => {
        await page.goto('/admin/benutzer');

        await expect(page.locator('[data-testid="user-row-testuser"]')).toBeVisible();
    });

    test('Neuen Benutzer anlegen: Modal öffnen, Formular ausfüllen und speichern', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Modal öffnen
        await page.locator('[data-testid="btn-new-user"]').click();
        await expect(page.locator('#modal-new-user')).not.toHaveClass(/hidden/);

        // Formular ausfüllen
        await page.locator('#modal-new-user #new-username').fill('e2e-testbenutzer');
        await page.locator('#modal-new-user #new-password').fill('E2EPasswort1!');
        await page.locator('#modal-new-user input[name="enabled"]').check();

        // Speichern (Form submit → Seite lädt neu)
        await page.locator('[data-testid="btn-submit-user"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        // Neuer Benutzer in der Liste
        await expect(page.locator('[data-testid="user-row-e2e-testbenutzer"]')).toBeVisible();
    });

    test('Benutzer bearbeiten: Edit-Modal öffnen, Flag ändern und speichern', async ({ page }) => {
        await page.goto('/admin/benutzer');

        // Edit-Button für testuser klicken
        await page.locator('[data-testid="btn-edit-user-testuser"]').click();
        await expect(page.locator('#modal-edit-user')).not.toHaveClass(/hidden/);

        // "Aktiv"-Checkbox umschalten (aktuell aktiv laut Seed)
        const enabledCb = page.locator('#modal-edit-user #edit-user-enabled');
        const wasChecked = await enabledCb.isChecked();
        if (wasChecked) {
            await enabledCb.uncheck();
        } else {
            await enabledCb.check();
        }

        // Speichern
        await page.locator('[data-testid="btn-submit-edit-user"]').click();
        await page.waitForURL(/\/admin\/benutzer/);

        // Zeile noch vorhanden nach Reload
        await expect(page.locator('[data-testid="user-row-testuser"]')).toBeVisible();
    });

    test('Benutzer löschen: Löschen-Button → Bestätigung → Zeile aus Tabelle entfernt', async ({ page }) => {
        // Dieser Test braucht einen eigenen Benutzer, der gelöscht werden kann.
        // Voraussetzung: "e2e-testbenutzer" wurde im vorangehenden Test angelegt.
        // Da Tests unabhängig sein sollen, legen wir ihn hier erneut an (idempotent via UI).
        await page.goto('/admin/benutzer');

        // Prüfen ob Benutzer bereits existiert; falls nicht anlegen
        const existingRow = page.locator('[data-testid="user-row-e2e-loeschtest"]');
        if (!(await existingRow.isVisible())) {
            await page.locator('[data-testid="btn-new-user"]').click();
            await page.locator('#modal-new-user #new-username').fill('e2e-loeschtest');
            await page.locator('#modal-new-user #new-password').fill('E2EPasswort1!');
            await page.locator('[data-testid="btn-submit-user"]').click();
            await page.waitForURL(/\/admin\/benutzer/);
        }

        // Löschen-Button klicken → HTML-Bestätigungsmodal öffnet sich
        await page.locator('[data-testid="btn-delete-user-e2e-loeschtest"]').click();
        await expect(page.locator('#modal-delete-user')).toBeVisible();
        await page.locator('[data-testid="btn-confirm-delete-user"]').click();

        // Nach Redirect: Benutzerliste ohne gelöschten Benutzer, Success-Banner sichtbar
        await page.waitForURL(/\/admin\/benutzer/);
        await expect(page.locator('.banner-success')).toBeVisible();
        await expect(page.locator('[data-testid="user-row-e2e-loeschtest"]')).not.toBeAttached();
    });

});
