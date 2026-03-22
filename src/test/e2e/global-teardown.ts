import { execSync } from 'child_process';
import * as path from 'path';

export default async function globalTeardown() {
    const composeFile = path.resolve(__dirname, 'docker-compose-e2e.yml');
    console.log('[global-teardown] Stoppe MySQL via Docker Compose…');
    execSync(`docker compose -f ${composeFile} down`, { stdio: 'inherit' });
    console.log('[global-teardown] MySQL gestoppt.');
}
