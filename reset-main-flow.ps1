$ErrorActionPreference = "Stop"

chcp 65001 | Out-Null
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "Resetting main-flow demo data..." -ForegroundColor Cyan
powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root "reset-demo-data.ps1") -Force
if ($LASTEXITCODE -ne 0) {
  throw "reset-demo-data.ps1 failed."
}

Write-Host "Main-flow demo data reset completed." -ForegroundColor Green
Write-Host "Next command for full interface regression: .\test-main-flow.ps1" -ForegroundColor Cyan
