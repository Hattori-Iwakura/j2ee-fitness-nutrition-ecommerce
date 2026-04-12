# Stops every process that is LISTENING on TCP port 8082 (e.g. old Spring Boot).
# Run from project root:  powershell -ExecutionPolicy Bypass -File .\scripts\kill-port-8082.ps1

$port = 8082
$conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if (-not $conns) {
    Write-Host "Port $port is free — nothing to stop."
    exit 0
}

$ids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($processId in $ids) {
    try {
        $p = Get-Process -Id $processId -ErrorAction Stop
        Write-Host "Stopping PID $processId ($($p.ProcessName))..."
        Stop-Process -Id $processId -Force
    }
    catch {
        Write-Warning "Could not stop PID ${processId}: $_"
    }
}

Write-Host "Done. Start the app with: .\mvnw.cmd spring-boot:run"
exit 0
