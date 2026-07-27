#
# MedManager v1.2 - build script
#
$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
. (Join-Path $here 'scripts\Resolve-Java.ps1')

$t = Get-MedManagerToolchain -ProjectRoot $here
$bin = Join-Path $here 'bin'

if (Test-Path $bin) { Remove-Item $bin -Recurse -Force }
New-Item -ItemType Directory $bin | Out-Null

$sources = Get-ChildItem -Recurse -Filter *.java (Join-Path $here 'src') | ForEach-Object { $_.FullName }

& $t.JavacExe `
    --module-path "$($t.JavaFxLib);$($t.PgJar)" `
    --add-modules javafx.controls,javafx.graphics `
    -d $bin `
    $sources

if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

$resSrc = Join-Path $here 'src\resources'
$resDst = Join-Path $bin 'resources'
if (Test-Path $resSrc) {
    New-Item -ItemType Directory $resDst -Force | Out-Null
    Copy-Item "$resSrc\*" $resDst -Recurse -Force
}

Write-Host "Build OK -> $bin"
