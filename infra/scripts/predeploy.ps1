# Pre-deploy script
$ErrorActionPreference = "Stop"

Write-Host "Running pre-deploy steps..." -ForegroundColor Green

$ROOT_DIR = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

# Build API container
Write-Host "Building API container..." -ForegroundColor Cyan
$apiDockerfile = Join-Path $ROOT_DIR "src/api/Dockerfile"
if (Test-Path $apiDockerfile) {
    Write-Host "API Dockerfile found - container will be built by azd deploy."
} else {
    Write-Host "No API Dockerfile found, skipping container build." -ForegroundColor Yellow
}

# Build Web container
Write-Host "Building Web container..." -ForegroundColor Cyan
$webDockerfile = Join-Path $ROOT_DIR "src/web/Dockerfile"
if (Test-Path $webDockerfile) {
    Write-Host "Web Dockerfile found - container will be built by azd deploy."
} else {
    Write-Host "No Web Dockerfile found, skipping container build." -ForegroundColor Yellow
}

Write-Host "Pre-deploy steps completed." -ForegroundColor Green
