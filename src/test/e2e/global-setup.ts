import * as fs from 'fs';
import * as path from 'path';

// MySQL wird im Maven-Lifecycle (pre-integration-test) via docker compose up gestartet,
// damit die DB bereit ist bevor Playwright seinen webServer (Spring Boot) hochfährt.
export default async function globalSetup() {
    // auth-state.json wird bei jedem Lauf zurückgesetzt, damit bei mehreren
    // sequenziellen Browser-Durchläufen (Chrome → Firefox) mit jeweils frischer
    // Datenbank keine veraltete Session wiederverwendet wird.
    // auth.setup.ts überschreibt sie anschließend mit der echten Session.
    const authState = path.resolve(__dirname, 'fixtures/auth-state.json');
    fs.writeFileSync(authState, JSON.stringify({ cookies: [], origins: [] }));
}
