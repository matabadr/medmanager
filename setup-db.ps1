#
# Create database medmandb and load schema + demo data.
# Requires PostgreSQL installed; psql on PATH or pass -Psql "C:\...\psql.exe"
#
param(
    [string]$DbName = 'medmandb',
    [string]$User = 'postgres',
    [string]$Password = '1234',
    [string]$Psql = 'psql'
)

$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
$schema = Join-Path $here 'db\schema.sql'
$seed   = Join-Path $here 'db\seed.sql'

if (-not (Test-Path $schema)) { throw "Missing $schema" }
if (-not (Get-Command $Psql -ErrorAction SilentlyContinue)) {
    throw "psql not found. Install PostgreSQL or pass -Psql `"C:\Program Files\PostgreSQL\16\bin\psql.exe`""
}

$env:PGPASSWORD = $Password

Write-Host "Checking database $DbName..."
$exists = & $Psql -U $User -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DbName'" 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Cannot connect to PostgreSQL as $User. Check service, password, or use -Password."
}
if ($exists.Trim() -ne '1') {
    Write-Host "Creating database $DbName..."
    & $Psql -U $User -d postgres -c "CREATE DATABASE $DbName;"
    if ($LASTEXITCODE -ne 0) { throw "CREATE DATABASE failed" }
}

Write-Host "Loading schema..."
& $Psql -U $User -d $DbName -f $schema
if ($LASTEXITCODE -ne 0) { throw "schema.sql failed" }

Write-Host "Loading seed data..."
& $Psql -U $User -d $DbName -f $seed
if ($LASTEXITCODE -ne 0) { throw "seed.sql failed" }

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
Write-Host ""
Write-Host "Database ready. Demo login: admin / admin123"
Write-Host "If your postgres password is not 1234, set DB_PASSWORD before .\run.ps1"
