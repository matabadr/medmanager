#
# MedManager v1.2 - launch script
# Env: DB_URL, DB_USER, DB_PASSWORD (defaults: localhost/medmandb, postgres, 1234)
#
$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
. (Join-Path $here 'scripts\Resolve-Java.ps1')

$t = Get-MedManagerToolchain -ProjectRoot $here
$bin = Join-Path $here 'bin'

if (-not (Test-Path (Join-Path $bin 'module-info.class'))) {
    Write-Host 'bin/ not found, building first...'
    & (Join-Path $here 'build.ps1')
}

& $t.JavaExe `
    --module-path "$($t.JavaFxLib);$bin;$($t.PgJar)" `
    --add-modules javafx.controls,javafx.graphics `
    --module medmanagerv1_1/main.Main
