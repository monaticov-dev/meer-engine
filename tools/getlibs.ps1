$ErrorActionPreference = 'Stop'
$ws = 'C:\Users\User\.zcode\workspace\734595c3-47ad-412d-b78a-25028ac5efe4'
$dst = Join-Path $ws 'runtime\libs'
New-Item -ItemType Directory -Force -Path $dst | Out-Null
$json = Get-Content (Join-Path $ws 'downloads\version-1.7.10.json') -Raw | ConvertFrom-Json
$failed = @()
foreach ($lib in $json.libraries) {
    $u = $lib.url
    if (-not $u) { $u = 'https://libraries.minecraft.net/' }
    # path part of maven coordinates
    $parts = $lib.name -split ':'
    $rel = "$($parts[0] -replace '\.','/')/$($parts[1])/$($parts[2])/$($parts[1])-$($parts[2]).jar"
    $out = Join-Path $dst ($parts[1] + '-' + $parts[2] + '.jar')
    if (Test-Path $out) { continue }
    try {
        Invoke-WebRequest ($u + $rel) -OutFile $out -UseBasicParsing -TimeoutSec 60 | Out-Null
        Write-Output ("OK  " + $lib.name)
    } catch {
        Write-Output ("ERR " + $lib.name + " : " + $_.Exception.Message)
        $failed += $lib.name
    }
}
Write-Output ("FAILED_COUNT=" + $failed.Count)
