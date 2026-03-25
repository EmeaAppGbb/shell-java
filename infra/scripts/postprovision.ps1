# Post-provision script
$ErrorActionPreference = "Stop"

Write-Host "Running post-provision steps..." -ForegroundColor Green

$ROOT_DIR = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

# Read environment variables from azd
$azdEnvOutput = azd env get-values
$envVars = @{}
foreach ($line in $azdEnvOutput) {
    if ($line -match '^([^=]+)=(.*)$') {
        $envVars[$matches[1]] = $matches[2] -replace '^"?(.*?)"?$', '$1'
    }
}

# Display provisioned resource info
Write-Host "Provisioned resources:" -ForegroundColor Cyan
Write-Host "  Container Registry: $($envVars['AZURE_CONTAINER_REGISTRY_ENDPOINT'])" -ForegroundColor Cyan
Write-Host "  API Resource ID:    $($envVars['AZURE_RESOURCE_API_ID'])" -ForegroundColor Cyan
Write-Host "  Web Resource ID:    $($envVars['AZURE_RESOURCE_WEB_ID'])" -ForegroundColor Cyan

Write-Host "Post-provision steps completed." -ForegroundColor Green
