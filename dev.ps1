# Dev helper: build mods, copy jars into MDK run/mods and start runClient (PowerShell)
# Usage: Open PowerShell in the workspace root and run: .\dev.ps1

$ErrorActionPreference = 'Stop'

Write-Host "Setting JAVA_HOME to JDK8..."
$Env:JAVA_HOME = 'C:\Users\narez\.jdks\jdk8u462-b08'
$Env:Path = "$Env:JAVA_HOME\bin;$Env:Path"

Write-Host "Java version:`n"; java -version

# Clear known corrupted ASM cache (optional)
$asmCache = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm\6.2"
if(Test-Path $asmCache){ Write-Host "Removing ASM 6.2 cache at $asmCache"; Remove-Item -Recurse -Force $asmCache }

# Build Permissions
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Write-Host "Workspace root: $root"

Write-Host "Building Permissions..."
$permDir = Join-Path $root '1.12.2\Permissions'
Push-Location $permDir
& .\gradlew.bat clean build --no-daemon --refresh-dependencies
Pop-Location

# Build Wallet
Write-Host "Building Wallet..."
$walletDir = Join-Path $root '1.12.2\Wallet'
Push-Location $walletDir
& .\gradlew.bat clean build --no-daemon --refresh-dependencies
Pop-Location

# Copy jars
$modsDir = Join-Path $root 'forge-1.12.2-mdk-test\run\mods'
New-Item -ItemType Directory -Path $modsDir -Force | Out-Null

 $permJar = Get-ChildItem -Path (Join-Path $permDir 'build\libs') -Filter *.jar | Select-Object -First 1
 $walletJar = Get-ChildItem -Path (Join-Path $walletDir 'build\libs') -Filter *.jar | Select-Object -First 1
if($permJar){ Copy-Item $permJar.FullName -Destination (Join-Path $modsDir 'permissions.jar') -Force }
if($walletJar){ Copy-Item $walletJar.FullName -Destination (Join-Path $modsDir 'wallet.jar') -Force }

Write-Host "Mods in ${modsDir}:";
Write-Host "";
Get-ChildItem $modsDir -File | Select-Object Name, Length | Format-Table

# Run client and capture log
Push-Location (Join-Path $root 'forge-1.12.2-mdk-test')
Write-Host "Starting runClient (log -> run_client.log)"
& .\gradlew.bat --no-daemon runClient > run_client.log 2>&1
Write-Host "runClient finished; last 80 lines of log:"
Get-Content run_client.log -Tail 80
Pop-Location

Write-Host "Done."

# Ensure EULA is accepted so runServer can start non-interactively
$eulaFile = Join-Path $root 'forge-1.12.2-mdk-test\run\eula.txt'
if(-not (Test-Path $eulaFile)){
	Write-Host "Creating run/eula.txt to auto-accept EULA"
	"eula=true" | Out-File -FilePath $eulaFile -Encoding ASCII
}

Write-Host "Starting runServer in background (log -> forge-1.12.2-mdk-test\run_server.log)"
Start-Process -FilePath (Join-Path $root 'forge-1.12.2-mdk-test\gradlew.bat') -ArgumentList '--no-daemon','runServer' -WorkingDirectory (Join-Path $root 'forge-1.12.2-mdk-test') -NoNewWindow | Out-Null
