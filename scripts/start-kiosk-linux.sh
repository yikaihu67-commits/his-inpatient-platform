#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080/api}"
KIOSK_URL="${KIOSK_URL:-http://localhost:5173/kiosk}"

echo "== Patient kiosk startup =="

if ! command -v node >/dev/null 2>&1; then
  echo "Node.js is not installed or not in PATH." >&2
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm is not installed or not in PATH." >&2
  exit 1
fi

if command -v curl >/dev/null 2>&1; then
  if curl -fsS "$BACKEND_URL/health" >/dev/null 2>&1; then
    echo "Backend health check passed: $BACKEND_URL/health"
  else
    echo "Warning: backend health check failed: $BACKEND_URL/health"
    echo "Start backend first, for example: cd backend && java -jar target/backend-0.0.1-SNAPSHOT.jar"
  fi
else
  echo "curl not found; backend health check skipped."
fi

cd "$FRONTEND_DIR"

if [ ! -d node_modules ]; then
  echo "Installing frontend dependencies..."
  npm install
fi

echo "Kiosk URL: $KIOSK_URL"
echo "Chromium kiosk command:"
echo "  chromium-browser --kiosk $KIOSK_URL"
echo ""
echo "Starting Vite dev server..."
npm run dev -- --host 0.0.0.0
