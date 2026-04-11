# Free 8080, start MySQL in Docker, then Spring Boot on http://localhost:8080
# Usage (repo root): powershell -ExecutionPolicy Bypass -File scripts\dev-run.ps1

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root

Write-Host "==> Free port 8080..."
& "$PSScriptRoot\dev-kill-8080.ps1"

Write-Host "==> Start MySQL (docker compose)..."
docker compose up -d mysql
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker failed. Start Docker Desktop, then run this script again."
    exit 1
}

Write-Host "==> Wait for MySQL healthy..."
$deadline = (Get-Date).AddSeconds(90)
do {
    Start-Sleep -Seconds 2
    $h = docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}" fitness-mysql 2>$null
    if ($h -eq "healthy") { Write-Host "MySQL is healthy."; break }
    Write-Host "   status: $h"
} while ((Get-Date) -lt $deadline)
if ($h -ne "healthy") {
    Write-Host "MySQL did not become healthy in time. Check: docker compose logs mysql"
    exit 1
}

Write-Host "==> Spring Boot (Ctrl+C to stop)..."
& "$root\mvnw.cmd" spring-boot:run
