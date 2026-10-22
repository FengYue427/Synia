param(
    [string]$InstanceDir = "$env:APPDATA\.minecraft\versions\neoforge-21.1.264"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "_common.ps1")

$gameDir = Resolve-GameDir $InstanceDir
$log = Join-Path $gameDir "logs\latest.log"
if (-not (Test-Path $log)) {
    throw "missing log: $log"
}

$outDir = Join-Path (Split-Path $PSScriptRoot -Parent) "out"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$out = Join-Path $outDir "snippets-$stamp.txt"

$lines = Get-Content $log -Encoding UTF8
$picked = New-Object System.Collections.Generic.List[string]

function Add-Matches([string]$pattern, [string]$label) {
    $hits = @($lines | Select-String -Pattern $pattern)
    $picked.Add("---- $label ($($hits.Count) hits) ----")
    if ($hits.Count -eq 0) {
        $picked.Add("(none)")
    } else {
        foreach ($h in $hits) { $picked.Add($h.Line) }
    }
    $picked.Add("")
}

Add-Matches "synia:debugify.*service_ts" "Synia service_ts"
Add-Matches "synia:debugify.*write_ts_" "Synia write_ts"
Add-Matches "synia:debugify.*disk_audit" "Synia disk_audit"
Add-Matches "synia:debugify.*Gate DISABLED" "Synia kill switch"
Add-Matches "Preloading Debugify" "Debugify preload"

$cfg = Join-Path $gameDir "config\debugify.json"
$picked.Add("---- config/debugify.json ----")
if (Test-Path $cfg) {
    $picked.Add((Get-Content $cfg -Raw -Encoding UTF8))
} else {
    $picked.Add("(missing)")
}

$utf8bom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllLines($out, $picked, $utf8bom)
Write-Host "wrote $out"
Get-Content $out -Encoding UTF8
