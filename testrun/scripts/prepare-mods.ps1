# 重新下载实测所需模组到 testrun/mods（含复制最新 synia jar）
$ErrorActionPreference = "Stop"
$ua = "Synia/0.1 (local-prep)"
$mods = Join-Path $PSScriptRoot "..\mods"
New-Item -ItemType Directory -Force -Path $mods | Out-Null

$downloads = @(
  @{ name = "connector-2.0.0-beta.17+1.21.1-full.jar"; url = "https://cdn.modrinth.com/data/u58R1TMW/versions/IITF0PRC/connector-2.0.0-beta.17%2B1.21.1-full.jar" },
  @{ name = "forgified-fabric-api-0.116.15+2.3.6+1.21.1.jar"; url = "https://cdn.modrinth.com/data/Aqlf1Shp/versions/C4eskNuR/forgified-fabric-api-0.116.15%2B2.3.6%2B1.21.1.jar" },
  @{ name = "Debugify-1.21.1+1.1.jar"; url = "https://cdn.modrinth.com/data/QwxR6Gcd/versions/pyIdu3f1/Debugify-1.21.1%2B1.1.jar" },
  @{ name = "yet_another_config_lib_v3-3.8.2+1.21.1-fabric.jar"; url = "https://cdn.modrinth.com/data/1eAoo2KR/versions/o3cDn8Vp/yet_another_config_lib_v3-3.8.2%2B1.21.1-fabric.jar" }
)

foreach ($d in $downloads) {
  $out = Join-Path $mods $d.name
  Write-Host "Downloading $($d.name) ..."
  Invoke-WebRequest -Uri $d.url -OutFile $out -Headers @{ "User-Agent" = $ua } -UseBasicParsing
}

$built = Join-Path $PSScriptRoot "..\..\patches\debugify\build\libs\synia-debugify-0.1.0.jar"
if (-not (Test-Path $built)) {
    throw "Missing synia-debugify jar. Build it with JDK 21: ./gradlew.bat :patches:debugify:jar"
}
Copy-Item $built (Join-Path $mods "synia-debugify-0.1.0.jar") -Force
Write-Host "OK -> $mods"
Get-ChildItem $mods | Format-Table Name, Length
