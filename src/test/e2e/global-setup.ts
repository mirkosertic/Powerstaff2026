import { execSync } from 'child_process';
import * as path from 'path';

export default async function globalSetup() {
    const composeFile = path.resolve(__dirname, 'docker-compose-e2e.yml');
    console.log('[global-setup] Starte MySQL via Docker Compose…');
    execSync(`docker compose -f ${composeFile} up -d --wait`, { stdio: 'inherit' });
    console.log('[global-setup] MySQL bereit.');
}
