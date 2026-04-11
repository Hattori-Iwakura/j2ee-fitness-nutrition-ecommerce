# Stops any process listening on TCP 8080 (e.g. leftover Spring Boot).
# Run from repo root: powershell -ExecutionPolicy Bypass -File scripts\dev-kill-8080.ps1

$ErrorActionPreference = 'SilentlyContinue'
$pids = @{}
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | ForEach-Object {
    if ($_.OwningProcess -gt 0) { $pids[$_.OwningProcess] = $true }
}
foreach ($procId in $pids.Keys) {
    $p = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($null -ne $p) {
        Write-Host "Stopping PID $procId ($($p.ProcessName)) on port 8080..."
        Stop-Process -Id $procId -Force
    }
}
if ($pids.Count -eq 0) {
    Write-Host "Nothing was listening on port 8080."
} else {
    Write-Host "Port 8080 should be free now."
}
