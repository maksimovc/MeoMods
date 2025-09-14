<#
Cleanup/archive generated files across MeoMods workspace.
Usage:
  # Dry-run (shows what will be moved)
  powershell -File .\scripts\cleanup-archive.ps1 -WhatIf

  # Perform cleanup
  powershell -File .\scripts\cleanup-archive.ps1

This script will move generated folders (build/, bin/, .gradle/, gradle-wrapper zips, wrapper files, and .class/.jar artifacts) into an archive folder with timestamp.
It will not delete anything.
#>

[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [string]$Version = '1.12.2'
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$archiveRoot = Join-Path $root (Join-Path 'archive' $stamp)

# Candidate patterns to archive within each mod/MDK
# Expand candidate patterns to archive within each mod/MDK
$patterns = @(
    'build',
    'bin',
    '.gradle',
    'gradle-2.3*',
    'gradle-*',
    'gradlew',
    'gradlew.bat',
    '*.zip',
    '*.jar',
    'runClient.launch',
    'runServer.launch',
    '.copying',
    '.placeholder'
)

# Determine targets based on requested version and support legacy layout
$targets = @()

# Preferred MDK path under mdks/<version>
$mdkCandidate = Join-Path $root (Join-Path 'mdks' (Join-Path $Version "forge-$Version-mdk-test"))
if (Test-Path $mdkCandidate) { $targets += $mdkCandidate }

# Legacy MDK path at top-level for compatibility
$legacyMdk = Join-Path $root "forge-$Version-mdk-test"
if (Test-Path $legacyMdk) { $targets += $legacyMdk }

# Target all modules under mods/<version> (preferred)
$modsDir = Join-Path $root (Join-Path 'mods' $Version)
if (Test-Path $modsDir) {
    $modules = Get-ChildItem -Path $modsDir -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    $targets += $modules
} else {
    # Legacy location where mods were under a top-level version folder
    $legacyMods = Join-Path $root $Version
    if (Test-Path $legacyMods) {
        $modules = Get-ChildItem -Path $legacyMods -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
        $targets += $modules
    }
}

if (-Not (Test-Path $archiveRoot)) {
    if ($PSCmdlet.ShouldProcess($archiveRoot, 'Create archive folder')) {
        New-Item -ItemType Directory -Path $archiveRoot | Out-Null
    }
}

foreach ($t in $targets) {
    foreach ($p in $patterns) {
    $items = Get-ChildItem -Path $t -Filter $p -Force -ErrorAction SilentlyContinue
    foreach ($it in $items) {
            $src = $it.FullName
            $rel = $src.Substring($root.Length).TrimStart('\')
            $dest = Join-Path $archiveRoot $rel
            $destDir = Split-Path -Parent $dest
            if ($PSCmdlet.ShouldProcess($src, "Move to archive $dest")) {
                if (-Not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir | Out-Null }
                Move-Item -Path $src -Destination $dest -Force
                Write-Host "Moved $src -> $dest"
            } else {
                Write-Host "WhatIf: would move $src -> $dest"
            }
        }
    }
}

Write-Host "Cleanup/archive complete. Archive created at: $archiveRoot"