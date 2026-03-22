import { test as setup } from '@playwright/test';
import { execSync } from 'child_process';
import { hashSync } from 'bcryptjs';
import * as path from 'path';

const AUTH_STATE = path.resolve(__dirname, '../fixtures/auth-state.json');
const FIXTURE_SQL = path.resolve(__dirname, '../fixtures/test-data.sql');

const DB_HOST = '127.0.0.1';
const DB_PORT = '3316';
const DB_USER = 'powerstaff';
const DB_PASS = 'powerstaff';
const DB_NAME = 'powerstaff_e2e';

function mysql(sql: string): void {
    execSync(
        `mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASS} ${DB_NAME} -e "${sql.replace(/"/g, '\\"')}"`,
        { stdio: 'inherit' }
    );
}

setup('Testdaten anlegen und einloggen', async ({ page }) => {
    // Testbenutzer anlegen (BCrypt-Hash für "testpass" wird zur Laufzeit berechnet)
    const hash = `{bcrypt}${hashSync('testpass', 10)}`;
    mysql(
        `INSERT INTO ps_user (username, password_hash, must_change_password, enabled) ` +
        `VALUES ('testuser', '${hash}', FALSE, TRUE) ` +
        `ON DUPLICATE KEY UPDATE password_hash = '${hash}', must_change_password = FALSE, enabled = TRUE`
    );

    // Stamm- und Testdaten einspielen
    execSync(
        `mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASS} ${DB_NAME} < ${FIXTURE_SQL}`,
        { stdio: 'inherit' }
    );

    // Login
    await page.goto('/login');
    await page.getByTestId('field-username').fill('testuser');
    await page.getByTestId('field-password').fill('testpass');
    await page.getByTestId('btn-login').click();
    await page.waitForURL('**/profilesearch/**');

    // Auth-State speichern
    await page.context().storageState({ path: AUTH_STATE });
});
