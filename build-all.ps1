$ErrorActionPreference = "Stop"

chcp 65001 | Out-Null
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

function Invoke-NativeStep {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Name,
    [Parameter(Mandatory = $true)]
    [scriptblock]$Script
  )

  Write-Host $Name -ForegroundColor Cyan
  & $Script
  if ($LASTEXITCODE -ne 0) {
    throw ("{0} failed with exit code {1}." -f $Name, $LASTEXITCODE)
  }
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

try {
  Set-Location (Join-Path $root "backend")
  Invoke-NativeStep -Name "Building backend ..." -Script { mvn -q -DskipTests package }
  Write-Host "后端构建通过" -ForegroundColor Green

  Set-Location (Join-Path $root "frontend")
  if (-not (Test-Path "node_modules")) {
    Invoke-NativeStep -Name "Installing frontend dependencies ..." -Script { npm install }
  }
  Invoke-NativeStep -Name "Building frontend ..." -Script { npm run build }
  Write-Host "前端构建通过" -ForegroundColor Green

  Set-Location $root
  Write-Host "全部构建完成" -ForegroundColor Green
}
catch {
  Set-Location $root
  Write-Host ("Build failed: {0}" -f $_.Exception.Message) -ForegroundColor Red
  exit 1
}
