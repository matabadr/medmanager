#
# Create a small zip (~2 MB) for sharing. Recipient runs setup-libs.ps1 once (~430 MB download).
#
param(
    [string]$Output = '',
    [switch]$IncludeDocs
)

$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
if (-not $Output) {
    $Output = Join-Path (Split-Path $here -Parent) 'medmanagerv1_1-share.zip'
}

$staging = Join-Path $env:TEMP "medmanagerv1_1-pack-$(Get-Random)"
if (Test-Path $staging) { Remove-Item $staging -Recurse -Force }
New-Item -ItemType Directory $staging | Out-Null

function Copy-ItemToStage([string]$Relative) {
    $src = Join-Path $here $Relative
    if (-not (Test-Path $src)) { return }
    $dst = Join-Path $staging $Relative
    $parent = Split-Path $dst -Parent
    if (-not (Test-Path $parent)) { New-Item -ItemType Directory $parent -Force | Out-Null }
    Copy-Item $src $dst -Recurse -Force
}

@(
    'src', 'db', 'scripts',
    'build.ps1', 'run.ps1', 'setup-libs.ps1', 'setup-db.ps1', 'package.ps1', 'SETUP.md',
    '.classpath', '.project', '.gitignore', '.settings'
) | ForEach-Object { Copy-ItemToStage $_ }

if ($IncludeDocs) { Copy-ItemToStage 'docs' }

$pgJar = Join-Path $here 'lib\postgresql-42.7.4.jar'
if (Test-Path $pgJar) {
    Copy-ItemToStage 'lib\postgresql-42.7.4.jar'
}

$sizeMb = [math]::Round((Get-ChildItem $staging -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB, 2)
Write-Host "Package contents: $sizeMb MB (uncompressed)"

if (Test-Path $Output) { Remove-Item $Output -Force }
Compress-Archive -Path "$staging\*" -DestinationPath $Output -CompressionLevel Optimal
Remove-Item $staging -Recurse -Force

$zipMb = [math]::Round((Get-Item $Output).Length / 1MB, 2)
Write-Host "Created: $Output ($zipMb MB)"
