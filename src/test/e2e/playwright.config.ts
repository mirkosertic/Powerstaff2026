import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const JAR = path.resolve(__dirname, '../../../target/powerstaff-1.0-SNAPSHOT.jar');
const PORT = 8100;

export default defineConfig({
    globalSetup: './global-setup.ts',
    globalTeardown: './global-teardown.ts',

    webServer: {
        command: `java -Dspring.profiles.active=e2e -Dserver.port=${PORT} -jar "${JAR}" > ../../../target/e2e-application.log 2>&1`,
        url: `http://localhost:${PORT}/actuator/health`,
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },

    use: {
        baseURL: `http://localhost:${PORT}`,
        locale: 'de-DE',
        timezoneId: 'Europe/Berlin',
        screenshot: 'on',
        video: 'on',
        trace: 'on-first-retry',
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

    reporter: [['html', { outputFolder: 'playwright-report' }]],
    retries: process.env.CI ? 1 : 0,
    workers: 1, // Sequentielle Ausführung, um Login-State-Isolation zu garantieren
    forbidOnly: !!process.env.CI,
});
