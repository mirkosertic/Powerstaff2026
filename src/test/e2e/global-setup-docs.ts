import * as fs from 'fs';
import * as path from 'path';
import { execSync } from 'child_process';

const AUTH_STATE = path.resolve(__dirname, 'fixtures/auth-state.json');
const COMPOSE_FILE = path.resolve(__dirname, 'docker-compose-e2e.yml');

// Startet MySQL via Docker Compose und setzt den Auth-State zurück.
// MySQL wird hier hochgefahren, weil die docs-Spec außerhalb des Maven-Lifecycles
// läuft (npx playwright test --config playwright.config.docs.ts) und deshalb
// kein Maven pre-integration-test zur Verfügung steht.
export default async function globalSetup() {
    console.log('Starting MySQL via Docker Compose...');
    execSync(`docker compose -f "${COMPOSE_FILE}" up -d --wait`, { stdio: 'inherit' });

    // Auth-State zurücksetzen, damit auth.setup.ts eine frische Session schreibt.
    fs.mkdirSync(path.dirname(AUTH_STATE), { recursive: true });
    fs.writeFileSync(AUTH_STATE, JSON.stringify({ cookies: [], origins: [] }));
}
