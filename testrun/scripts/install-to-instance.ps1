param(
    [Parameter(Mandatory = $true)]
    [string]$InstanceDir
)

$ErrorActionPreference = "Stop"

function Resolve-GameDir([string]$dir) {
    $d = (Resolve-Path $dir).Path
    if (Test-Path (Join-Path $d "mods")) { return $d }
    if (Test-Path (Join-Path $d ".minecraft\mods")) { return (Join-Path $d ".minecraft") }
    if (Test-Path (Join-Path $d "minecraft\mods")) { return (Join-Path $d "minecraft") }
    throw "在 '$d' 下找不到 mods 文件夹。请指向实例游戏目录（含 mods 的那一层）。"
}

$modsSrc = Join-Path $PSScriptRoot "..\mods"
$gameDir = Resolve-GameDir $InstanceDir
$modsDst = Join-Path $gameDir "mods"
New-Item -ItemType Directory -Force -Path $modsDst | Out-Null

Write-Host "游戏目录: $gameDir"
Write-Host "导入模组 -> $modsDst"
Get-ChildItem $modsSrc -Filter *.jar | ForEach-Object {
    Copy-Item $_.FullName (Join-Path $modsDst $_.Name) -Force
    Write-Host "  + $($_.Name)"
}

$configDebugify = Join-Path $gameDir "config\debugify.json"
if (Test-Path $configDebugify) {
    $bak = "$configDebugify.bak-synia"
    Copy-Item $configDebugify $bak -Force
    Remove-Item $configDebugify -Force
    Write-Host "已备份并清除旧 config/debugify.json -> $bak"
}

Write-Host ""
Write-Host "完成。下一步: .\group-A.ps1 -InstanceDir `"$InstanceDir`""
