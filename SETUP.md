# MedManager v1.2 — Installation (recipient)

## Quick start

1. Unzip the project folder.
2. Open PowerShell in that folder and run:

```powershell
.\setup-libs.ps1    # once: downloads JDK + JavaFX (~430 MB, needs internet)
.\setup-db.ps1      # once: creates DB + tables + demo users (needs PostgreSQL)
.\build.ps1
.\run.ps1
```

3. Log in with **admin** / **admin123** (see `db/seed.sql` for more accounts).

## Requirements

| Component | Notes |
|-----------|--------|
| Windows 10/11 | PowerShell scripts |
| Internet | Only for `setup-libs.ps1` (first time) |
| PostgreSQL | [Download](https://www.postgresql.org/download/windows/). Default app login: `postgres` / `1234` on database `medmandb` |

If PostgreSQL uses another password:

```powershell
.\setup-db.ps1 -Password "your_password"
$env:DB_PASSWORD = "your_password"
.\run.ps1
```

## Already have Java 21?

Skip `setup-libs.ps1` if you have:

- **JDK 21+** on `PATH` or `JAVA_HOME`
- **JavaFX 21 SDK** — set `JAVA_FX_HOME` to the SDK folder (parent of `lib\javafx.controls.jar`)

You still need `lib\postgresql-42.7.4.jar` (included in the zip, or run `setup-libs.ps1` which fetches it).

## Sender: create the small zip

```powershell
.\package.ps1
```

Produces `medmanagerv1_1-share.zip` (~2 MB) on your Desktop.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| JDK / JavaFX not found | Run `.\setup-libs.ps1` |
| PostgreSQL hors-ligne | Start PostgreSQL service; run `.\setup-db.ps1` |
| psql not found | `.\setup-db.ps1 -Psql "C:\Program Files\PostgreSQL\16\bin\psql.exe"` |
| Login fails | Re-run `.\setup-db.ps1` |
