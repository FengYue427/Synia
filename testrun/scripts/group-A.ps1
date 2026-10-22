param(
    [string]$InstanceDir = "$env:APPDATA\.minecraft\versions\neoforge-21.1.264"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "_common.ps1")

$gameDir = Resolve-GameDir $InstanceDir
$modsDir = Join-Path $gameDir "mods"
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null

Write-Host "GROUP A: gate ON"
Ensure-BaseMods $modsDir
Install-SyniaJar $modsDir
Reset-DebugifyConfig $gameDir
Write-Host "Do NOT add -Dsynia.debugify.enabled"
Write-Host "Launch, break blocks, quit, then run collect-snippets.ps1"
