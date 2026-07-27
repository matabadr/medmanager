#
# Download JDK 21 + JavaFX 21 SDK into lib/ (one-time, ~430 MB, needs internet).
# Skip if you already have them in lib/ or use JAVA_HOME + JAVA_FX_HOME.
#
$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Definition

$jdkDir = Join-Path $here 'lib\jdk-21.0.5+11'
$fxDir  = Join-Path $here 'lib\javafx-sdk-21.0.5'
$pgJar  = Join-Path $here 'lib\postgresql-42.7.4.jar'

New-Item -ItemType Directory (Join-Path $here 'lib') -Force | Out-Null

if (-not (Test-Path $pgJar)) {
    Write-Host "Downloading PostgreSQL JDBC driver..."
    $jdbcUrl = 'https://jdbc.postgresql.org/download/postgresql-42.7.4.jar'
    Invoke-WebRequest -Uri $jdbcUrl -OutFile $pgJar -UseBasicParsing
}

if (-not (Test-Path (Join-Path $jdkDir 'bin\java.exe'))) {
    Write-Host "Downloading JDK 21 (Temurin, ~190 MB)..."
    $jdkZip = Join-Path $env:TEMP 'medmanager-jdk21.zip'
    $jdkUrl = 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip'
    Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing

    $jdkExtract = Join-Path $env:TEMP 'medmanager-jdk-extract'
    if (Test-Path $jdkExtract) { Remove-Item $jdkExtract -Recurse -Force }
    Expand-Archive -Path $jdkZip -DestinationPath $jdkExtract -Force

    $inner = Get-ChildItem $jdkExtract -Directory | Select-Object -First 1
    if (Test-Path $jdkDir) { Remove-Item $jdkDir -Recurse -Force }
    New-Item -ItemType Directory (Split-Path $jdkDir) -Force | Out-Null
    Move-Item $inner.FullName $jdkDir
    Remove-Item $jdkZip, $jdkExtract -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "JDK installed -> $jdkDir"
}
else {
    Write-Host "JDK already present."
}

if (-not (Test-Path (Join-Path $fxDir 'lib\javafx.controls.jar'))) {
    Write-Host "Downloading JavaFX 21 SDK (~45 MB zip)..."
    $fxZip = Join-Path $env:TEMP 'medmanager-javafx.zip'
    $fxUrl = 'https://download2.gluonhq.com/openjfx/21.0.5/openjfx-21.0.5_windows-x64_bin-sdk.zip'
    Invoke-WebRequest -Uri $fxUrl -OutFile $fxZip -UseBasicParsing

    $fxExtract = Join-Path $env:TEMP 'medmanager-fx-extract'
    if (Test-Path $fxExtract) { Remove-Item $fxExtract -Recurse -Force }
    Expand-Archive -Path $fxZip -DestinationPath $fxExtract -Force

    $inner = Get-ChildItem $fxExtract -Directory | Where-Object { $_.Name -like 'javafx-sdk*' } | Select-Object -First 1
    if (-not $inner) { $inner = Get-ChildItem $fxExtract -Directory | Select-Object -First 1 }
    if (Test-Path $fxDir) { Remove-Item $fxDir -Recurse -Force }
    Move-Item $inner.FullName $fxDir
    Remove-Item $fxZip, $fxExtract -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "JavaFX installed -> $fxDir"
}
else {
    Write-Host "JavaFX already present."
}

Write-Host ""
Write-Host "Dependencies OK. Next: .\setup-db.ps1  then  .\build.ps1  .\run.ps1"
