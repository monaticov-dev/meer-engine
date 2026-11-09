$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$ws = 'C:\Users\User\.zcode\workspace\734595c3-47ad-412d-b78a-25028ac5efe4'
$assets = Join-Path $ws 'runtime\assets'
New-Item -ItemType Directory -Force -Path $assets | Out-Null

$idxUrl = 'https://launchermeta.mojang.com/v1/packages/1863782e33ce7b584fc45b037325a1964e095d3e/1.7.10.json'
$idxPath = Join-Path $assets '1.7.10.json'
Invoke-WebRequest $idxUrl -OutFile $idxPath -UseBasicParsing | Out-Null
$idx = Get-Content $idxPath -Raw | ConvertFrom-Json

$http = New-Object System.Net.Http.HttpClient
$http.Timeout = [TimeSpan]::FromSeconds(60)
$http.DefaultRequestHeaders.Add('User-Agent', 'meer-build')

$props = $idx.objects.PSObject.Properties
$total = ($props | Measure-Object).Count
$n = 0
foreach ($p in $props) {
    $hash = $p.Value.hash
    $sub = $hash.Substring(0, 2)
    $dir = Join-Path $assets "objects\$sub"
    $file = Join-Path $dir $hash
    $n++
    if (Test-Path $file) { continue }
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $url = "https://resources.download.minecraft.net/$sub/$hash"
    try {
        $bytes = $http.GetByteArrayAsync($url).GetAwaiter().GetResult()
        [IO.File]::WriteAllBytes($file, $bytes)
    } catch {
        Write-Output ("ERR " + $hash + " " + $_.Exception.Message)
    }
    if ($n % 200 -eq 0) { Write-Output ("progress " + $n + "/" + $total) }
}
Write-Output ("DONE " + $total)
