<#
Collect built JARs from each project's build/libs directory into releases/ with consistent names.
Usage: .\scripts\collect-jars.ps1 [-WhatIf]
#>

[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [string]$Version = '1.12.2'
)


$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

$releases = Join-Path $root 'releases'
if (-Not (Test-Path $releases)) {
    New-Item -ItemType Directory -Path $releases | Out-Null
}

# Find module folders in mods/<version> and mdks/<version>
$modules = Get-ChildItem -Path (Join-Path $root (Join-Path 'mods' $Version)) -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
$mdkDir = Join-Path $root (Join-Path 'mdks' $Version)
$mdkSubs = @()
if (Test-Path $mdkDir) { Get-ChildItem -Path $mdkDir -Directory -ErrorAction SilentlyContinue | ForEach-Object { $mdkSubs += $_.FullName } }
$projects = @()
foreach ($m in $modules) { $projects += $m }
foreach ($m in $mdkSubs) { $projects += $m }

foreach ($proj in $projects) {
    $libs = Join-Path $proj 'build\libs'
    if (-Not (Test-Path $libs)) { continue }
    Get-ChildItem -Path $libs -Filter '*.jar' -File -ErrorAction SilentlyContinue | ForEach-Object {
        $src = $_.FullName
        $name = $_.Name
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $destName = "$($proj.Split('\')[-1])-$stamp-$name"
        $dest = Join-Path $releases $destName
        # Determine whether to actually copy or just show (WhatIf)
        if ($null -ne $PSCmdlet) {
            $do = $PSCmdlet.ShouldProcess($src, "Copy to $dest")
        } else {
            # Fallback: if WhatIfPreference is set, treat as dry-run
            $do = -not ($WhatIfPreference -ne $null -and $WhatIfPreference -ne '')
        }

        if ($do) {
            Copy-Item -Path $src -Destination $dest -Force
            Write-Host "Copied $src -> $dest"
        } else {
            Write-Host "WhatIf: would copy $src -> $dest"
        }
    }
}

Write-Host "Collect finished. JARs are in $releases"
