#!/bin/bash
set -e

echo -e "\033[0;32mRunning post-provision steps...\033[0m"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Read environment variables from azd
eval "$(azd env get-values)"

# Display provisioned resource info
echo -e "\033[0;36mProvisioned resources:\033[0m"
echo -e "  Container Registry: ${AZURE_CONTAINER_REGISTRY_ENDPOINT:-not set}"
echo -e "  API Resource ID:    ${AZURE_RESOURCE_API_ID:-not set}"
echo -e "  Web Resource ID:    ${AZURE_RESOURCE_WEB_ID:-not set}"

echo -e "\033[0;32mPost-provision steps completed.\033[0m"
