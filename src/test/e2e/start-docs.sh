#!/bin/bash
set -e

# Docs screenshot startup script (Dev-Mode, kein JaCoCo)
# Findet das Spring-Boot-JAR automatisch im target/-Verzeichnis.
# Voraussetzung: ./mvnw package -DskipTests wurde ausgeführt.
# Usage: bash start-docs.sh [PORT]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

PORT="${1:-8200}"
JAR=$(find "${PROJECT_ROOT}/target" -maxdepth 1 -name "*.jar" ! -name "*-sources.jar" 2>/dev/null | head -1)
PID_FILE="${PROJECT_ROOT}/target/docs-app.pid"

# Validation
if [ -z "$JAR" ]; then
  echo "ERROR: No JAR found in ${PROJECT_ROOT}/target/. Run './mvnw package -DskipTests' first." >&2
  exit 1
fi

echo "Starting docs app: $JAR on port $PORT"

# Forward SIGTERM/SIGINT to the JVM so it shuts down cleanly.
cleanup() {
  if [ -n "$APP_PID" ]; then
    kill -TERM "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  rm -f "$PID_FILE"
}
trap cleanup TERM INT

# Start JVM with dev mode enabled (realistic seed data, no JaCoCo overhead).
java \
  -Dspring.profiles.active=e2e \
  -Ddevmode=true \
  -Dserver.port="$PORT" \
  -jar "$JAR" &

APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

# Block until the JVM exits.
wait "$APP_PID"
