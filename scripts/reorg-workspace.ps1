<#
Reorganize workspace into multi-version layout.
Usage:
  # Dry-run
  powershell -File .\scripts\reorg-workspace.ps1 -Version 1.12.2 -WhatIf

  # Perform
  powershell -File .\scripts\reorg-workspace.ps1 -Version 1.12.2

This will:
- create folders: mods/<version>/, mdks/<version>/, releases/<version>/, tools/
- move existing 1.12.2 mod folders from 1.12.2/ to mods/1.12.2/
- move forge-1.12.2-mdk-test and forge-1.12.2-mdk-clean to mdks/1.12.2/
- move existing releases/* into releases/<version>/
#>
[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [string]$Version = '1.12.2'
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

$modsSrc = Join-Path $root '1.12.2'
$modsDstRoot = Join-Path $root (Join-Path 'mods' $Version)
$mdksDstRoot = Join-Path $root (Join-Path 'mdks' $Version)
$releasesDst = Join-Path $root (Join-Path 'releases' $Version)

# Ensure targets exist
if ($PSCmdlet.ShouldProcess($modsDstRoot, 'Create mods destination')) { New-Item -ItemType Directory -Path $modsDstRoot -Force | Out-Null }
if ($PSCmdlet.ShouldProcess($mdksDstRoot, 'Create mdks destination')) { New-Item -ItemType Directory -Path $mdksDstRoot -Force | Out-Null }
if ($PSCmdlet.ShouldProcess($releasesDst, 'Create releases destination')) { New-Item -ItemType Directory -Path $releasesDst -Force | Out-Null }
if ($PSCmdlet.ShouldProcess((Join-Path $root 'tools'), 'Create tools folder')) { New-Item -ItemType Directory -Path (Join-Path $root 'tools') -Force | Out-Null }

# Move mods
if (Test-Path $modsSrc) {
    Get-ChildItem -Path $modsSrc -Directory | ForEach-Object {
        $src = $_.FullName
        $dst = Join-Path $modsDstRoot $_.Name
        if ($PSCmdlet.ShouldProcess($src, "Move mod $_.Name to $dst")) {
            Move-Item -Path $src -Destination $dst -Force
            Write-Host "Moved mod $_.Name -> $dst"
        } else {
            Write-Host "WhatIf: would move mod $_.Name -> $dst"
        }
    }
}

# Move mdks
$mdkTest = Join-Path $root 'forge-1.12.2-mdk-test'
$mdkClean = Join-Path $root 'forge-1.12.2-mdk-clean'
if (Test-Path $mdkTest) {
    $dst = Join-Path $mdksDstRoot 'forge-1.12.2-mdk-test'
    if ($PSCmdlet.ShouldProcess($mdkTest, "Move mdk-test to $dst")) { Move-Item -Path $mdkTest -Destination $dst -Force; Write-Host "Moved mdk-test -> $dst" } else { Write-Host "WhatIf: would move mdk-test -> $dst" }
}
if (Test-Path $mdkClean) {
    $dst = Join-Path $mdksDstRoot 'forge-1.12.2-mdk-clean'
    if ($PSCmdlet.ShouldProcess($mdkClean, "Move mdk-clean to $dst")) { Move-Item -Path $mdkClean -Destination $dst -Force; Write-Host "Moved mdk-clean -> $dst" } else { Write-Host "WhatIf: would move mdk-clean -> $dst" }
}

# Move releases content
$releasesSrc = Join-Path $root 'releases'
if (Test-Path $releasesSrc) {
    Get-ChildItem -Path $releasesSrc -File | ForEach-Object {
        $src = $_.FullName
        $dst = Join-Path $releasesDst $_.Name
        if ($PSCmdlet.ShouldProcess($src, "Move release $_.Name to $dst")) { Move-Item -Path $src -Destination $dst -Force; Write-Host "Moved release $_.Name -> $dst" } else { Write-Host "WhatIf: would move release $_.Name -> $dst" }
    }
}

Write-Host "Reorganization for version $Version complete. New layout under mods/ and mdks/."
