import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const SCRIPT = path.resolve(__dirname, 'start-docs.sh');
const LOG = path.resolve(__dirname, '../../../target/docs-application.log');
const PORT = 8200;

export default defineConfig({
    globalSetup: './global-setup-docs.ts',
    globalTeardown: './global-teardown-docs.ts',

    webServer: {
        command: `bash "${SCRIPT}" ${PORT} > "${LOG}" 2>&1`,
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
            fullPage: true,
        },
        storageState: 'fixtures/auth-state.json',
    },

    projects: [
        {
            name: 'setup',
            testMatch: /auth\.setup\.ts/,
            use: { storageState: undefined },
            timeout: 60_000,
        },
        {
            name: 'docs',
            testMatch: /docs\.spec\.ts/,
            use: {
                ...devices['Desktop Chrome'],
                viewport: { width: 1280, height: 1024 },
            },
            dependencies: ['setup'],
        },
    ],

    reporter: [['html', { open: 'never', outputFolder: '../../target/docs-report' }]],
    workers: 1,
    forbidOnly: !!process.env.CI,
});
