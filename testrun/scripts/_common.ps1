function Resolve-GameDir([string]$dir) {
    $d = (Resolve-Path $dir).Path
    if (Test-Path (Join-Path $d "mods")) { return $d }
    if (Test-Path (Join-Path $d ".minecraft\mods")) { return (Join-Path $d ".minecraft") }
    if (Test-Path (Join-Path $d "minecraft\mods")) { return (Join-Path $d "minecraft") }
    throw "mods folder not found under $d"
}

function Get-SyniaJarSource {
    $built = Join-Path $PSScriptRoot "..\..\patches\debugify\build\libs\synia-debugify-0.1.0.jar"
    $bundled = Join-Path $PSScriptRoot "..\mods\synia-debugify-0.1.0.jar"
    if (Test-Path $built) { return (Resolve-Path $built).Path }
    if (Test-Path $bundled) { return (Resolve-Path $bundled).Path }
    throw "synia-debugify jar not found"
}

function Ensure-BaseMods([string]$modsDir) {
    $src = Join-Path $PSScriptRoot "..\mods"
    $required = @(
        "connector-2.0.0-beta.17+1.21.1-full.jar",
        "forgified-fabric-api-0.116.15+2.3.6+1.21.1.jar",
        "Debugify-1.21.1+1.1.jar"
    )
    foreach ($name in $required) {
        $from = Join-Path $src $name
        if (-not (Test-Path $from)) { throw "missing preset mod: $from" }
        Copy-Item $from (Join-Path $modsDir $name) -Force
    }
}

function Remove-SyniaJars([string]$modsDir) {
    Get-ChildItem $modsDir -Filter "synia-debugify*.jar" -ErrorAction SilentlyContinue |
        ForEach-Object { Remove-Item $_.FullName -Force; Write-Host "removed $($_.Name)" }
}

function Install-SyniaJar([string]$modsDir) {
    Remove-SyniaJars $modsDir
    $src = Get-SyniaJarSource
    $dst = Join-Path $modsDir "synia-debugify-0.1.0.jar"
    Copy-Item $src $dst -Force
    Write-Host "installed synia-debugify-0.1.0.jar"
}

function Reset-DebugifyConfig([string]$gameDir) {
    $cfg = Join-Path $gameDir "config\debugify.json"
    if (Test-Path $cfg) {
        Remove-Item $cfg -Force
        Write-Host "deleted config/debugify.json"
    }
}
