#!/bin/bash
set -e
cd src/api && mvn dependency:resolve && cd ../..
cd src/web && npm ci && cd ../..
npx playwright install-deps && npx playwright install
pip install mkdocs mkdocs-material
