param(
    [string]$InstanceDir = "$env:APPDATA\.minecraft\versions\neoforge-21.1.264"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "_common.ps1")

$gameDir = Resolve-GameDir $InstanceDir
$modsDir = Join-Path $gameDir "mods"

Write-Host "GROUP C: no Synia jar"
Ensure-BaseMods $modsDir
Remove-SyniaJars $modsDir
Reset-DebugifyConfig $gameDir
Write-Host "Remove -Dsynia.debugify.enabled=false if present, then launch"
