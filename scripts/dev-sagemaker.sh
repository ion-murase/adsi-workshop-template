#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== Stopping existing processes ==="
fuser -k 3000/tcp 2>/dev/null || true
fuser -k 3001/tcp 2>/dev/null || true
fuser -k 8080/tcp 2>/dev/null || true
sleep 1

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH=/codeeditor/default/absports/3000

echo "=== Building frontend ==="
cd "$PROJECT_ROOT/packages/frontend"
npx next build

echo "=== Starting backend (workshop profile, H2) ==="
cd "$PROJECT_ROOT/packages/backend"
./gradlew bootRun --args='--spring.profiles.active=workshop' > /tmp/attendance-backend.log 2>&1 &
echo "Backend PID: $!"

echo "=== Starting Next.js ==="
cd "$PROJECT_ROOT/packages/frontend"
npx next start -H 127.0.0.1 -p 3001 > /tmp/attendance-frontend.log 2>&1 &
echo "Next.js PID: $!"

sleep 2

echo "=== Starting SageMaker proxy ==="
node "$PROJECT_ROOT/packages/frontend/scripts/sagemaker-proxy.mjs" > /tmp/attendance-proxy.log 2>&1 &
echo "Proxy PID: $!"

echo ""
echo "=== All services started ==="
echo "  Backend:  http://localhost:8080"
echo "  Next.js:  http://localhost:3001"
echo "  Proxy:    http://localhost:3000"
echo ""
echo "Open in browser:"
echo "  PORTS tab -> port 3000 globe icon -> replace 'ports' with 'absports'"
echo ""
echo "Stop with: npm run dev:sagemaker:stop"
