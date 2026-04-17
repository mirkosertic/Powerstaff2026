import * as fs from 'fs';
import * as path from 'path';
import { execSync } from 'child_process';

const PID_FILE = path.resolve(__dirname, '../../../target/docs-app.pid');
const COMPOSE_FILE = path.resolve(__dirname, 'docker-compose-e2e.yml');

export default async function globalTeardown() {
    // Spring Boot App beenden
    try {
        const pid = parseInt(fs.readFileSync(PID_FILE, 'utf-8').trim(), 10);
        if (pid && !isNaN(pid)) {
            try {
                process.kill(pid, 'SIGTERM');
            } catch {
                // Prozess bereits beendet
            }

            // Warten bis der Prozess weg ist (max. 15s)
            const deadline = Date.now() + 15_000;
            while (Date.now() < deadline) {
                try {
                    process.kill(pid, 0);
                    await new Promise(r => setTimeout(r, 200));
                } catch {
                    break;
                }
            }
        }
    } catch {
        // PID-Datei nicht vorhanden
    }

    // MySQL via Docker Compose stoppen
    console.log('Stopping MySQL via Docker Compose...');
    try {
        execSync(`docker compose -f "${COMPOSE_FILE}" down`, { stdio: 'inherit' });
    } catch {
        // Ignorieren falls bereits gestoppt
    }
}
