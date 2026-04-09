import { test as setup } from '@playwright/test';
import * as path from 'path';

const AUTH_STATE = path.resolve(__dirname, '../fixtures/auth-state.json');

// Testdaten werden via Flyway (db/testdata/V100__e2e_seed.sql) beim App-Start eingespielt.
// Testbenutzer: testuser / testpass (Passwort als {noop}testpass in der DB gespeichert).
setup('Einloggen und Auth-State speichern', async ({ page }) => {
    // Erste Navigation kann länger dauern wegen App-Initialisierung
    await page.goto('/login', { waitUntil: 'networkidle', timeout: 60_000 });
    await page.getByTestId('field-username').fill('testuser');
    await page.getByTestId('field-password').fill('testpass');
    await page.getByTestId('btn-login').click();
    await page.waitForURL('**/freelancer/**');
    await page.context().storageState({ path: AUTH_STATE });
});
