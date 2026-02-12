#!/bin/bash
set -euo pipefail

# SGCD-PM Production Deploy Script
# Usage: ./deploy.sh [user@host]

SERVER="${1:-root@217.154.2.230}"
PROJECT_DIR="/opt/sgcd-pm"
COMPOSE_FILE="docker-compose.prod.yml"

echo "=== SGCD-PM Production Deploy ==="
echo "Target: $SERVER:$PROJECT_DIR"
echo ""

# Check .env.prod exists
if [ ! -f .env.prod ]; then
    echo "ERROR: .env.prod not found. Create it first."
    exit 1
fi

# 1. Sync project files to server
echo "[1/4] Syncing project files..."
rsync -avz --delete \
    --exclude node_modules \
    --exclude target \
    --exclude .git \
    --exclude .angular \
    --exclude dist \
    --exclude '.env*' \
    --exclude '*.zip' \
    ./ "$SERVER:$PROJECT_DIR/"

# 2. Copy production env file
echo "[2/4] Copying production environment..."
scp .env.prod "$SERVER:$PROJECT_DIR/.env"

# 3. Build and start containers
echo "[3/4] Building and starting containers..."
ssh "$SERVER" "cd $PROJECT_DIR && docker compose -f $COMPOSE_FILE up --build -d"

# 4. Verify health
echo "[4/4] Verifying deployment..."
sleep 5

ssh "$SERVER" "cd $PROJECT_DIR && docker compose -f $COMPOSE_FILE ps"

echo ""
echo "=== Deployment complete ==="
echo "App: http://217.154.2.230/"
echo "API: http://217.154.2.230/api/v1/auth/login"
echo "Stakeholder: http://217.154.2.230/stakeholder?token=sgcd-stakeholder-2026"
