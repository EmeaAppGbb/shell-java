#!/bin/bash
set -e

echo -e "\033[0;32mRunning pre-deploy steps...\033[0m"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Build API container
echo -e "\033[0;36mBuilding API container...\033[0m"
if [ -f "$ROOT_DIR/src/api/Dockerfile" ]; then
    cd "$ROOT_DIR/src/api"
    echo "API Dockerfile found — container will be built by azd deploy."
else
    echo -e "\033[0;33mNo API Dockerfile found, skipping container build.\033[0m"
fi

# Build Web container
echo -e "\033[0;36mBuilding Web container...\033[0m"
if [ -f "$ROOT_DIR/src/web/Dockerfile" ]; then
    cd "$ROOT_DIR/src/web"
    echo "Web Dockerfile found — container will be built by azd deploy."
else
    echo -e "\033[0;33mNo Web Dockerfile found, skipping container build.\033[0m"
fi

echo -e "\033[0;32mPre-deploy steps completed.\033[0m"
