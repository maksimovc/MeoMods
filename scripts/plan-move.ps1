<# Plan moves for reorganizing workspace into multi-version layout
   This script prints planned moves (dry-run) for review.
   Run it to review, then I'll perform actual moves.
#>

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $root

Write-Host "Planned moves (dry-run):"
Write-Host "- '1.12.2/*' -> 'mods/1.12.2/*'"
Write-Host "- 'forge-1.12.2-mdk-test' -> 'mdks/1.12.2/forge-1.12.2-mdk-test'"
Write-Host "- 'forge-1.12.2-mdk-clean' -> 'mdks/1.12.2/forge-1.12.2-mdk-clean'"
Write-Host "- 'releases/*' -> 'releases/1.12.2/*'"
Write-Host "- create 'tools/' for local Gradle"

Write-Host "Run cleanup and real moves with the organizer script when ready."
