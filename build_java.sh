#!/bin/bash
set -e

SSH_USER="root"
SSH_HOST="165.22.247.142"
REMOTE_DIR="/opt/backend/emenu"

echo "======================================"
echo "🚀 Starting Maven Build (DEV ONLY)"
echo "======================================"

# Optional Maven build
mvn clean package -DskipTests

# Write private key to a temp file
KEY_FILE=$(mktemp)
cat > "$KEY_FILE" << 'KEYEOF'
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACDnVkVCQxQlhxK/cL4Bx/R+w+zluvY/GFa2VJcrqQX7ywAAAJCLX5hRi1+Y
UQAAAAtzc2gtZWQyNTUxOQAAACDnVkVCQxQlhxK/cL4Bx/R+w+zluvY/GFa2VJcrqQX7yw
AAAED2nI6DcctOecuQ+rib8vdfLCqubU0fnz2jfyZfn6So7+dWRUJDFCWHEr9wvgHH9H7D
7OW69j8YVrZUlyupBfvLAAAADGRpZ2l0YWxvY2VhbgE=
-----END OPENSSH PRIVATE KEY-----
KEYEOF

chmod 600 "$KEY_FILE"

echo "======================================"
echo "🚀 Connecting to DEV server..."
echo "======================================"

ssh -o StrictHostKeyChecking=no -i "$KEY_FILE" "$SSH_USER@$SSH_HOST" << 'EOF'
export TERM=xterm
cd /opt/backend/emenu
bash deploy-emenu.sh
EOF

# Cleanup
rm -f "$KEY_FILE"

echo "======================================"
echo "🎉 DEV DEPLOY COMPLETED"
echo "======================================"
