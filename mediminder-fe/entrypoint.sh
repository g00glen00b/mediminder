#!/usr/bin/env sh
set -eu
envsubst '${API_URL}' < /etc/nginx/conf.d/nginx.conf.template > /etc/nginx/conf.d/default.conf
exec "$@"
