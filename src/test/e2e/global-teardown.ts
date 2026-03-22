// MySQL wird im Maven-Lifecycle (post-integration-test) via docker compose down gestoppt.
// globalTeardown bleibt als leerer Hook für künftige Playwright-spezifische Aufräumarbeiten.
export default async function globalTeardown() {}
