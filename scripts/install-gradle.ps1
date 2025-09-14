<#
Install a local Gradle distribution under tools/gradle.

Usage:
  # Dry-run
  powershell -File .\scripts\install-gradle.ps1 -WhatIf

  # Install Gradle 6.9.4 (default)
  powershell -File .\scripts\install-gradle.ps1

Optional parameters:
  -Version <version>  (default: 6.9.4)

This script will backup an existing tools/gradle folder (if present) and create
tools/gradle containing the Gradle distribution. It downloads from services.gradle.org.
#>

[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [string]$Version = '6.9.4'
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

$tools = Join-Path $root 'tools'
if (-Not (Test-Path $tools)) {
    if ($PSCmdlet.ShouldProcess($tools, 'Create tools folder')) { New-Item -ItemType Directory -Path $tools | Out-Null }
}

$dest = Join-Path $tools 'gradle'
if (Test-Path $dest) {
    $backup = Join-Path $tools ('gradle.backup.' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    if ($PSCmdlet.ShouldProcess($dest, "Backup existing gradle to $backup")) { Rename-Item -Path $dest -NewName (Split-Path $backup -Leaf) }
}

$zipName = "gradle-$Version-bin.zip"
$url = "https://services.gradle.org/distributions/$zipName"
$zipPath = Join-Path $tools $zipName

if ($PSCmdlet.ShouldProcess($url, "Download $zipName")) {
    Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
}

if ($PSCmdlet.ShouldProcess($zipPath, 'Extract Gradle')) {
    Expand-Archive -LiteralPath $zipPath -DestinationPath $tools -Force
    $extracted = Join-Path $tools ("gradle-$Version")
    if (Test-Path $extracted) { Rename-Item -Path $extracted -NewName 'gradle' }
    Remove-Item $zipPath -Force
}

Write-Host "Gradle installed at: $dest"
