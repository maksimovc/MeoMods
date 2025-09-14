<#
List JARs in releases and print SHA256 checksums.
Usage: .\scripts\list-releases.ps1 [-Version '1.12.2']
#>

[CmdletBinding()]
param(
    [string]$Version = '1.12.2',
    [ValidateSet('text','json','csv')]
    [string]$OutFormat = 'text',
    [string]$OutFile
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

$releases = Join-Path $root 'releases'
if (-Not (Test-Path $releases)) {
    Write-Host "No releases directory found at $releases"
    exit 0
}

# Optionally filter by version substring in filename
$pattern = if ($Version) { $Version } else { '' }

 $items = Get-ChildItem -Path $releases -Filter '*.jar' -File | Where-Object { $_.Name -like "*$pattern*" } | ForEach-Object {
    $file = $_.FullName
    $name = $_.Name
    $hash = Get-FileHash -Path $file -Algorithm SHA256
    [PSCustomObject]@{
        FileName = $name
        Path = $file
        SHA256 = $hash.Hash
        Size = $_.Length
        LastWriteTime = $_.LastWriteTime
    }
}

if ($OutFormat -eq 'text') {
    foreach ($it in $items) { Write-Host "$($it.FileName)`t$($it.SHA256)" }
} elseif ($OutFormat -eq 'json') {
    $out = if ($OutFile) { $OutFile } else { Join-Path $releases ("manifest-{0:yyyyMMdd-HHmmss}.json" -f (Get-Date)) }
    $items | ConvertTo-Json -Depth 5 | Out-File -FilePath $out -Encoding UTF8
    Write-Host "Wrote JSON manifest to $out"
} elseif ($OutFormat -eq 'csv') {
    $out = if ($OutFile) { $OutFile } else { Join-Path $releases ("manifest-{0:yyyyMMdd-HHmmss}.csv" -f (Get-Date)) }
    $items | Export-Csv -Path $out -NoTypeInformation -Encoding UTF8
    Write-Host "Wrote CSV manifest to $out"
}
