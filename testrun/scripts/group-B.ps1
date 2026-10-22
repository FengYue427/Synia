param(
    [string]$InstanceDir = "$env:APPDATA\.minecraft\versions\neoforge-21.1.264"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "_common.ps1")

$gameDir = Resolve-GameDir $InstanceDir
$modsDir = Join-Path $gameDir "mods"

Write-Host "GROUP B: kill switch"
Ensure-BaseMods $modsDir
Install-SyniaJar $modsDir
Reset-DebugifyConfig $gameDir
Write-Host "Add this JVM arg, then launch:"
Write-Host "  -Dsynia.debugify.enabled=false"
