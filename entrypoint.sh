#!/bin/sh

# Set default values for environment variables
export NGINX_SERVER_NAME=${NGINX_SERVER_NAME:-localhost}
export NGINX_MAX_BODY=${NGINX_MAX_BODY:-10M}
export HOST=${HOST:-app}
export PORT=${PORT:-8080}

# Create nginx configuration from template
envsubst '${NGINX_SERVER_NAME},${NGINX_MAX_BODY},${HOST},${PORT}' < /etc/nginx/templates/vhost.template > /etc/nginx/conf.d/default.conf

# Start nginx
nginx -g "daemon off;"