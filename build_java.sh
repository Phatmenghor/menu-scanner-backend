#!/bin/bash

set -e

SSH_USER="root"
SSH_HOST="165.22.247.142"
REMOTE_DIR="/opt/backend/emenu"

echo "======================================"
echo "🚀 Starting Maven Build (DEV ONLY)"
echo "======================================"

mvn clean package -DskipTests

echo "======================================"
echo "✅ Build SUCCESSFUL"
echo "🚀 Connecting to DEV server..."
echo "======================================"

ssh \
  -o StrictHostKeyChecking=no \
  -i <(cat << 'KEYEOF'
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACDnVkVCQxQlhxK/cL4Bx/R+w+zluvY/GFa2VJcrqQX7ywAAAJCLX5hRi1+Y
UQAAAAtzc2gtZWQyNTUxOQAAACDnVkVCQxQlhxK/cL4Bx/R+w+zluvY/GFa2VJcrqQX7yw
AAAED2nI6DcctOecuQ+rib8vdfLCqubU0fnz2jfyZfn6So7+dWRUJDFCWHEr9wvgHH9H7D
7OW69j8YVrZUlyupBfvLAAAADGRpZ2l0YWxvY2VhbgE=
-----END OPENSSH PRIVATE KEY-----
KEYEOF
) "$SSH_USER@$SSH_HOST" << 'EOF'
export TERM=xterm
cd /opt/backend/emenu
bash deploy-emenu.sh
EOF

echo "======================================"
echo "🎉 DEV DEPLOY COMPLETED"
echo "======================================"
