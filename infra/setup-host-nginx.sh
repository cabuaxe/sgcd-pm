#!/bin/bash
set -euo pipefail

# SGCD VPS Host Nginx Setup
# Run on the VPS as root: bash setup-host-nginx.sh [domain]
#
# Examples:
#   bash setup-host-nginx.sh sgcd-consular.de
#   bash setup-host-nginx.sh 217.154.2.230.nip.io   (nip.io for IP-based subdomains)

DOMAIN="${1:?Usage: $0 <domain>}"
NGINX_CONF="/etc/nginx/sites-available/sgcd"

echo "=== SGCD Host Nginx Setup ==="
echo "Domain: $DOMAIN"
echo ""

# 1. Install nginx if not present
if ! command -v nginx &> /dev/null; then
    echo "[1/5] Installing nginx..."
    apt-get update -qq && apt-get install -y -qq nginx
else
    echo "[1/5] nginx already installed"
fi

# 2. Install certbot if not present
if ! command -v certbot &> /dev/null; then
    echo "[2/5] Installing certbot..."
    apt-get install -y -qq certbot python3-certbot-nginx
else
    echo "[2/5] certbot already installed"
fi

# 3. Write nginx config with actual domain
echo "[3/5] Writing nginx config to $NGINX_CONF..."
sed "s/<DOMAIN>/$DOMAIN/g" /opt/sgcd-pm/infra/nginx-host.conf > "$NGINX_CONF"

# 4. Enable site and remove default
ln -sf "$NGINX_CONF" /etc/nginx/sites-enabled/sgcd
rm -f /etc/nginx/sites-enabled/default

# 5. Test and reload
echo "[4/5] Testing nginx config..."
nginx -t

echo "[5/5] Reloading nginx..."
systemctl reload nginx

echo ""
echo "=== Host nginx configured ==="
echo "PM:  http://pm.$DOMAIN/"
echo "MVP: http://app.$DOMAIN/ (when available)"
echo "IP:  http://217.154.2.230/ (fallback → PM)"
echo ""
echo "To add SSL, run:"
echo "  certbot --nginx -d pm.$DOMAIN -d app.$DOMAIN"
