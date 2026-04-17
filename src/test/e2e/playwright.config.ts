import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const SCRIPT = path.resolve(__dirname, '../../../target/start-e2e-with-jacoco.sh');
const PORT = 8100;
const MGMT_PORT = 8090; // management.server.port in application.yml

export default defineConfig({
    globalSetup: './global-setup.ts',
    globalTeardown: './global-teardown.ts',

    webServer: {
        command: `bash "${SCRIPT}" ${PORT} > ../../../target/e2e-application.log 2>&1`,
        // /login is in permitAll() → reliably returns HTTP 200 without authentication.
        // Actuator health on port 8090 requires auth (SecurityConfig.anyRequest().authenticated()).
        url: `http://localhost:${PORT}/login`,
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },

    use: {
        baseURL: `http://localhost:${PORT}`,
        locale: 'de-DE',
        timezoneId: 'Europe/Berlin',
        screenshot: {
            mode: 'on',
            fullPage: true,  // Fullpage-Screenshots für lange Seiten
        },
        video: 'on',
        trace: 'retain-on-failure',  // Traces bei Fehlern für besseres Debugging
        storageState: 'fixtures/auth-state.json',
    },

    projects: [
        // Login-Fixture läuft zuerst, ohne storageState
        {
            name: 'setup',
            testMatch: /auth\.setup\.ts/,
            use: { storageState: undefined },
            timeout: 60_000, // 60s für initialen App-Start
        },
        // Alle Admin-Tests: Chromium mit fester Auflösung 1280×1024
        {
            name: 'chromium',
            testIgnore: /admin-noadmin\.spec\.ts/,
            use: {
                ...devices['Desktop Chrome'],
                viewport: { width: 1280, height: 1024 },
            },
            dependencies: ['setup'],
        },
        // Firefox mit gleicher Auflösung
        {
            name: 'firefox',
            testIgnore: /admin-noadmin\.spec\.ts/,
            use: {
                ...devices['Desktop Firefox'],
                viewport: { width: 1280, height: 1024 },
            },
            dependencies: ['setup'],
        },
        // Nicht-Admin-Tests: Chromium, läuft sequenziell NACH allen Admin-Tests
        {
            name: 'non-admin-chromium',
            testMatch: /admin-noadmin\.spec\.ts/,
            use: {
                ...devices['Desktop Chrome'],
                viewport: { width: 1280, height: 1024 },
                storageState: undefined, // Kein gespeicherter Login
            },
            dependencies: ['setup'],
        },
        // Nicht-Admin-Tests: Firefox, läuft sequenziell NACH allen Admin-Tests
        {
            name: 'non-admin-firefox',
            testMatch: /admin-noadmin\.spec\.ts/,
            use: {
                ...devices['Desktop Firefox'],
                viewport: { width: 1280, height: 1024 },
                storageState: undefined, // Kein gespeicherter Login
            },
            dependencies: ['setup'],
        },
    ],

    reporter: [['html', { open: 'never' }]],
    retries: process.env.CI ? 1 : 0,
    workers: 1, // Sequentielle Ausführung, um Login-State-Isolation zu garantieren
    forbidOnly: !!process.env.CI,
});
