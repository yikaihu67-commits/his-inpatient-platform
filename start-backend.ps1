$ErrorActionPreference = "Stop"

chcp 65001 | Out-Null
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Stop-PortOwner {
  param(
    [Parameter(Mandatory = $true)]
    [int]$Port
  )

  $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  if (-not $connections) {
    return
  }

  $ownerIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique
  foreach ($ownerId in $ownerIds) {
    if (-not $ownerId -or $ownerId -eq $PID) {
      continue
    }

    $process = Get-Process -Id $ownerId -ErrorAction SilentlyContinue
    $processName = if ($process) { $process.ProcessName } else { "unknown" }
    Write-Host ("Port {0} is occupied by PID {1} ({2}). Stopping it..." -f $Port, $ownerId, $processName) -ForegroundColor Yellow

    try {
      Stop-Process -Id $ownerId -Force -ErrorAction Stop
    }
    catch {
      throw ("Failed to stop process PID {0} on port {1}: {2}" -f $ownerId, $Port, $_.Exception.Message)
    }
  }

  Start-Sleep -Seconds 2
  $remaining = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  if ($remaining) {
    $remainingIds = ($remaining | Select-Object -ExpandProperty OwningProcess -Unique) -join ", "
    throw ("Port {0} is still occupied by PID(s): {1}. Please close those processes and retry." -f $Port, $remainingIds)
  }
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Stop-PortOwner -Port 8080

Set-Location (Join-Path $root "backend")
Write-Host "Starting backend at http://localhost:8080 ..." -ForegroundColor Cyan
mvn spring-boot:run
if ($LASTEXITCODE -ne 0) {
  throw ("Backend startup command failed with exit code {0}." -f $LASTEXITCODE)
}
