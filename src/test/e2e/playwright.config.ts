import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const JAR = path.resolve(__dirname, '../../../target/powerstaff-1.0-SNAPSHOT.jar');
const PORT = 8090;

export default defineConfig({
    globalSetup: './global-setup.ts',
    globalTeardown: './global-teardown.ts',

    webServer: {
        command: `java -jar "${JAR}" --server.port=${PORT} --spring.profiles.active=e2e`,
        url: `http://localhost:${PORT}/actuator/health`,
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },

    use: {
        baseURL: `http://localhost:${PORT}`,
        locale: 'de-DE',
        timezoneId: 'Europe/Berlin',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure',
        trace: 'on-first-retry',
        storageState: 'fixtures/auth-state.json',
    },

    projects: [
        // Login-Fixture läuft zuerst, ohne storageState
        {
            name: 'setup',
            testMatch: /auth\.setup\.ts/,
            use: { storageState: undefined },
        },
        // Alle anderen Tests: Chromium mit fester Auflösung 1280×1024
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome'],
                viewport: { width: 1280, height: 1024 },
            },
            dependencies: ['setup'],
        },
    ],

    reporter: [['html', { outputFolder: 'playwright-report' }]],
    retries: process.env.CI ? 1 : 0,
    workers: process.env.CI ? 1 : 2,
    forbidOnly: !!process.env.CI,
});
