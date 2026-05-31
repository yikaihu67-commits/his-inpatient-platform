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
    Write-Host ("Port {0} is occupied by PID {1} ({2}). Stopping it before starting Vite..." -f $Port, $ownerId, $processName) -ForegroundColor Yellow

    try {
      Stop-Process -Id $ownerId -Force -ErrorAction Stop
    }
    catch {
      Write-Host ("Failed to stop PID {0}: {1}" -f $ownerId, $_.Exception.Message) -ForegroundColor Yellow
      Write-Host "Vite may choose another available port automatically." -ForegroundColor Yellow
    }
  }

  Start-Sleep -Seconds 1
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $root "frontend")

if (-not (Test-Path "node_modules")) {
  Write-Host "Installing frontend dependencies ..." -ForegroundColor Cyan
  npm install
  if ($LASTEXITCODE -ne 0) {
    throw ("npm install failed with exit code {0}." -f $LASTEXITCODE)
  }
}

Stop-PortOwner -Port 5173

Write-Host "Starting frontend at http://localhost:5173 ..." -ForegroundColor Cyan
npm run dev
if ($LASTEXITCODE -ne 0) {
  throw ("Frontend startup command failed with exit code {0}." -f $LASTEXITCODE)
}
