# Build all MeoMods using the MDK test wrapper
# Usage: .\scripts\build-all.ps1 [-Clean]
[CmdletBinding(SupportsShouldProcess=$true)]
param(
    [switch]$Clean,
    [switch]$Local,
    [string]$Version = '1.12.2'
)

# The script lives in MeoMods\scripts; workspace root is the parent directory of the scripts folder
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Set-Location $root

Write-Host "Building all mods in MeoMods workspace (MDK location: mdks/<version>/ or legacy forge-<version>-mdk-test/ )"

# Paths: prefer mdks/<version>/forge-<version>-mdk-test, fall back to previous location
# Determine MDK locations (prefer mdks/<version>/forge-<version>-mdk-test)
$mdkDirCandidate = Join-Path $root (Join-Path 'mdks' $Version)
$mdkDirCandidate = Join-Path $mdkDirCandidate "forge-$Version-mdk-test"
$mdkWrapperCandidate = Join-Path $mdkDirCandidate "gradlew.bat"
if (Test-Path $mdkDirCandidate) {
    $mdkDir = $mdkDirCandidate
    if (Test-Path $mdkWrapperCandidate) { $wrapper = $mdkWrapperCandidate }
} else {
    # fallback for older layout at repository root
    $legacyMdk = Join-Path $root "forge-$Version-mdk-test"
    if (Test-Path $legacyMdk) {
        $mdkDir = $legacyMdk
        $wrapper = Join-Path $legacyMdk "gradlew.bat"
    } else {
        $mdkDir = $mdkDirCandidate # still set to candidate (may not exist)
    }
}

# Optionally prefer a local Gradle distribution at tools/gradle/bin/gradle.bat
$localGradle = Join-Path $root 'tools\gradle\bin\gradle.bat'

# Decide which gradle command to run
if ($Local) {
    if (Test-Path $localGradle) {
        $gradleCmd = $localGradle
        # When using local Gradle, run it from the repository root so the top-level settings.gradle applies
        $cmd = "Push-Location `"$root`"; & `"$gradleCmd`" "
        Write-Host "Using local Gradle from repository root: $localGradle"
    } else {
        Write-Error "Local Gradle requested (-Local) but not found at: $localGradle"
        exit 1
    }
} elseif (Test-Path $wrapper) {
    $gradleCmd = $wrapper
    # When using the MDK wrapper, run it from the MDK directory (preserve MDK semantics)
    $cmd = "Push-Location `"$mdkDir`"; & `"$gradleCmd`" "
    Write-Host "Using MDK wrapper: $wrapper"
} else {
    Write-Error "No gradle wrapper or local gradle found. Expected either $wrapper or $localGradle"
    exit 1
}

if ($Clean) { $cmd += "clean " }
$cmd += "build --no-daemon"

# After running, ensure we pop the location back
$cmd += "; Pop-Location"

Write-Host "Prepared command: $cmd"
if ($PSCmdlet.ShouldProcess($gradleCmd, 'Run Gradle build')) {
    Write-Host "Running: $cmd"
    Invoke-Expression $cmd
} else {
    Write-Host "WhatIf/dry-run: not executing the build."
}

Write-Host "Build finished. JARs are in each project's build/libs directory."