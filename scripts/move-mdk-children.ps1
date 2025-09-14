$src='C:\Users\narez\Desktop\MeoMods\forge-1.12.2-mdk-test'
$dst='C:\Users\narez\Desktop\MeoMods\mdks\1.12.2\forge-1.12.2-mdk-test'
if(-not(Test-Path $dst)){ New-Item -ItemType Directory -Path $dst | Out-Null }
Get-ChildItem -Path $src -Force -Name | ForEach-Object {
    $s=Join-Path $src $_
    $d=Join-Path $dst $_
    try {
        Move-Item -Path $s -Destination $d -Force -ErrorAction Stop
        Write-Host "Moved child $_"
    } catch {
        Write-Host "Could not move $_ : $($_.Exception.Message)"
    }
}