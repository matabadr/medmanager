function Test-JavaVersion21Plus {
    param([string]$JavaExe)
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = (& $JavaExe -version 2>&1 | Out-String)
    } finally {
        $ErrorActionPreference = $prev
    }
    if ($out -match 'version "(\d+)') {
        return [int]$Matches[1] -ge 21
    }
    return $false
}

function Get-MedManagerToolchain {
    param([Parameter(Mandatory)][string]$ProjectRoot)

    $pgJar = Join-Path $ProjectRoot 'lib\postgresql-42.7.4.jar'
    if (-not (Test-Path $pgJar)) {
        throw "Missing JDBC driver: lib\postgresql-42.7.4.jar"
    }

    $bundledJdk = Join-Path $ProjectRoot 'lib\jdk-21.0.5+11'
    $bundledFx  = Join-Path $ProjectRoot 'lib\javafx-sdk-21.0.5\lib'

    $javaExe  = $null
    $javacExe = $null

    if (Test-Path (Join-Path $bundledJdk 'bin\java.exe')) {
        $javaExe  = Join-Path $bundledJdk 'bin\java.exe'
        $javacExe = Join-Path $bundledJdk 'bin\javac.exe'
    }
    elseif ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        $javaExe  = Join-Path $env:JAVA_HOME 'bin\java.exe'
        $javacExe = Join-Path $env:JAVA_HOME 'bin\javac.exe'
    }
    else {
        $javaCmd = Get-Command java -ErrorAction SilentlyContinue
        if ($javaCmd) {
            $javaExe = $javaCmd.Source
            $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
            if ($javacCmd) { $javacExe = $javacCmd.Source }
        }
    }

    if (-not $javaExe -or -not (Test-Path $javaExe)) {
        throw @"
JDK 21 not found.
  Run:  .\setup-libs.ps1
  Or install JDK 21 and set JAVA_HOME, or add java to PATH.
"@
    }
    if (-not (Test-JavaVersion21Plus $javaExe)) {
        $prev = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
        $ver = (& $javaExe -version 2>&1 | Select-Object -First 1)
        $ErrorActionPreference = $prev
        throw "Java 21+ required. Found: $ver"
    }
    if (-not $javacExe -or -not (Test-Path $javacExe)) {
        throw "javac not found (install a JDK, not only a JRE)."
    }

    $javaFxLib = $null
    if (Test-Path $bundledFx) {
        $javaFxLib = $bundledFx
    }
    elseif ($env:JAVA_FX_HOME) {
        $fx = Join-Path $env:JAVA_FX_HOME 'lib'
        if (Test-Path $fx) { $javaFxLib = $fx }
    }

    if (-not $javaFxLib) {
        throw @"
JavaFX SDK not found.
  Run:  .\setup-libs.ps1
  Or download JavaFX 21 SDK and set JAVA_FX_HOME to its folder (the one that contains lib\).
"@
    }

    [PSCustomObject]@{
        JavaExe     = $javaExe
        JavacExe    = $javacExe
        JavaFxLib   = $javaFxLib
        PgJar       = $pgJar
        ProjectRoot = $ProjectRoot
    }
}
