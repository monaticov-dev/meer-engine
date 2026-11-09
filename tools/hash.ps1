param([string]$Root = "C:\Users\User\.zcode\workspace\734595c3-47ad-412d-b78a-25028ac5efe4\downloads")
$targets = 'client-1.7.10.jar','static-merged.jar','forge-1.7.10-installer.jar','mcp_stable-12-1.7.10.zip','vineflower.jar','specialsource.jar','xz.jar','jdk21.zip','jdk27.zip','version-1.7.10.json'
foreach ($f in $targets) {
    $p = Join-Path $Root $f
    if (Test-Path $p) {
        $s  = (Get-Item $p).Length
        $h1 = (Get-FileHash $p -Algorithm SHA1).Hash.ToLower()
        $h2 = (Get-FileHash $p -Algorithm SHA256).Hash.ToLower()
        Write-Output ("{0} | size={1} | sha1={2} | sha256={3}" -f $f, $s, $h1, $h2)
    } else {
        Write-Output ("{0} | MISSING" -f $f)
    }
}
