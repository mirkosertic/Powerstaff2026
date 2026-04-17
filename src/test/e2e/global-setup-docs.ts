import * as fs from 'fs';
import * as path from 'path';

const AUTH_STATE = path.resolve(__dirname, 'fixtures/auth-state.json');

// MySQL wird jetzt direkt in start-docs.sh hochgefahren (vor dem JVM-Start),
// damit die Reihenfolge unabhängig vom Playwright-internen Setup-Timing ist.
export default async function globalSetup() {
    // Auth-State zurücksetzen, damit auth.setup.ts eine frische Session schreibt.
    fs.mkdirSync(path.dirname(AUTH_STATE), { recursive: true });
    fs.writeFileSync(AUTH_STATE, JSON.stringify({ cookies: [], origins: [] }));
}
