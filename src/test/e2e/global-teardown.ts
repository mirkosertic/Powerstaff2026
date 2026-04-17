import * as fs from 'fs';
import * as path from 'path';

const PID_FILE = path.resolve(__dirname, '../../../target/e2e-app.pid');

// Send SIGTERM directly to the JVM (PID written by start-e2e-with-jacoco.sh) and
// wait until the process is gone. This guarantees the JaCoCo shutdown hook has
// flushed jacoco-e2e.exec before Playwright itself exits and Maven proceeds to
// the jacoco:report-e2e goal.
export default async function globalTeardown() {
    let pid: number;
    try {
        pid = parseInt(fs.readFileSync(PID_FILE, 'utf-8').trim(), 10);
        if (!pid || isNaN(pid)) return;
    } catch {
        return; // PID file not found – nothing to kill
    }

    // Send SIGTERM; the bash wrapper's trap will forward it if the PID is the
    // JVM directly, or the JVM receives it directly here.
    try {
        process.kill(pid, 'SIGTERM');
    } catch {
        return; // Already exited
    }

    // Wait for the JVM to exit (= JaCoCo has written jacoco-e2e.exec).
    const deadline = Date.now() + 30_000;
    while (Date.now() < deadline) {
        try {
            process.kill(pid, 0); // signal 0 = existence check
            await new Promise(r => setTimeout(r, 200));
        } catch {
            return; // Process gone – JaCoCo file was written
        }
    }
}
