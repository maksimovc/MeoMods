<#
Create a new mod from templates/mod-template
Usage: .\scripts\create-mod.ps1 -Name MyMod -Version 1.12.2
#>
param(
    [Parameter(Mandatory=$true)] [string]$Name,
    [string]$Version = '1.12.2'
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
$template = Join-Path $root 'templates\mod-template'
$target = Join-Path $root (Join-Path "mods\$Version" $Name)

if (Test-Path $target) {
    Write-Error "Target already exists: $target"
    exit 1
}

Copy-Item -Path $template -Destination $target -Recurse

# Replace placeholders in files
Get-ChildItem -Path $target -Recurse -File | ForEach-Object {
    (Get-Content $_.FullName) -replace 'modid', $Name.ToLower() -replace 'ModName', $Name | Set-Content $_.FullName
}

Write-Host "Created mod scaffold at: $target"
