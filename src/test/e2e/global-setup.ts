import * as fs from 'fs';
import * as path from 'path';

// MySQL wird im Maven-Lifecycle (pre-integration-test) via docker compose up gestartet,
// damit die DB bereit ist bevor Playwright seinen webServer (Spring Boot) hochfährt.
export default async function globalSetup() {
    // auth-state.json muss existieren bevor Playwright die Browser-Kontexte initialisiert.
    // auth.setup.ts überschreibt sie anschließend mit der echten Session.
    const authState = path.resolve(__dirname, 'fixtures/auth-state.json');
    if (!fs.existsSync(authState)) {
        fs.writeFileSync(authState, JSON.stringify({ cookies: [], origins: [] }));
    }
}
