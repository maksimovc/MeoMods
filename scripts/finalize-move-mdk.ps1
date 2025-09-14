$src='C:\Users\narez\Desktop\MeoMods\forge-1.12.2-mdk-test'
$dst='C:\Users\narez\Desktop\MeoMods\mdks\1.12.2\forge-1.12.2-mdk-test'
if(-not(Test-Path $dst)){ New-Item -ItemType Directory -Path $dst | Out-Null }

$items = Get-ChildItem -Path $src -Force -ErrorAction SilentlyContinue
foreach($it in $items) {
    $s = $it.FullName
    $d = Join-Path $dst $it.Name
    try {
        Move-Item -LiteralPath $s -Destination $d -Force -ErrorAction Stop
        Write-Host "Moved: $s -> $d"
    } catch {
        try {
            Copy-Item -LiteralPath $s -Destination $d -Recurse -Force -ErrorAction Stop
            Write-Host "Copied (fallback): $s -> $d"
        } catch {
            Write-Host "Failed to move or copy: $s -> $d : $($_.Exception.Message)"
        }
    }
}
# attempt to remove source folder if now empty
try {
    Remove-Item -LiteralPath $src -Recurse -Force -ErrorAction Stop
    Write-Host "Removed empty source mdK folder"
} catch {
    Write-Host "Source mdK folder left in place (likely locked)"
}
