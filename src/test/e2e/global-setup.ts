// MySQL wird im Maven-Lifecycle (pre-integration-test) via docker compose up gestartet,
// damit die DB bereit ist bevor Playwright seinen webServer (Spring Boot) hochfährt.
// globalSetup bleibt als leerer Hook für künftige Playwright-spezifische Initialisierung.
export default async function globalSetup() {}
