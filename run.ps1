<#
.SYNOPSIS
    Start the RunEase application on the embedded Tomcat server.

.DESCRIPTION
    Builds the project with the Maven wrapper and launches the application on
    http://localhost:8080 (or the port given with -Port).

    The database is created automatically on first run in ./data using H2.
    Delete the ./data folder to rebuild the schema and reload the demo data.

.PARAMETER Port
    HTTP port to listen on. Defaults to 8080. Can also be set with the PORT
    environment variable.

.PARAMETER Package
    Build the deployable WAR (target/runease.war) instead of starting the server.

.EXAMPLE
    .\run.ps1

.EXAMPLE
    .\run.ps1 -Port 9090

.EXAMPLE
    .\run.ps1 -Package
#>
[CmdletBinding()]
param(
    [int]$Port = 0,
    [switch]$Package
)

$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

# --- Locate a JDK -----------------------------------------------------------
function Find-Jdk {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }

    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd -and $javaCmd.Source -notmatch 'javapath') {
        $candidates += Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
    }

    foreach ($root in @("$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium",
                        "$env:ProgramFiles\Microsoft", "$env:ProgramFiles\Amazon Corretto")) {
        if (Test-Path $root) {
            $candidates += Get-ChildItem $root -Directory -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending | ForEach-Object { $_.FullName }
        }
    }

    foreach ($candidate in $candidates) {
        if (Test-Path (Join-Path $candidate "bin\javac.exe")) { return $candidate }
    }
    return $null
}

if (-not $env:JAVA_HOME) {
    $jdk = Find-Jdk
    if (-not $jdk) {
        Write-Host "Java Development Kit (JDK 21) was not found." -ForegroundColor Red
        Write-Host "Install a JDK and set JAVA_HOME, then run this script again."
        exit 1
    }
    $env:JAVA_HOME = $jdk
}

# --- Locate Maven -----------------------------------------------------------
$mvn = Join-Path $PSScriptRoot "mvnw.cmd"
if (-not (Test-Path $mvn)) {
    $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $mvnCmd) {
        Write-Host "Maven was not found." -ForegroundColor Red
        Write-Host "Install Maven 3.9+ or keep the bundled mvnw.cmd wrapper in this folder."
        exit 1
    }
    $mvn = $mvnCmd.Source
}

if ($Port -eq 0) {
    $Port = if ($env:PORT) { [int]$env:PORT } else { 8080 }
}
$env:PORT = "$Port"

if ($Package) {
    Write-Host "Building target/runease.war ..." -ForegroundColor Cyan
    & $mvn -B clean package
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "  RunEase - Running Event Management System" -ForegroundColor Cyan
Write-Host "  URL         : http://localhost:$Port/"
Write-Host "  Admin       : admin@runease.my      / Password123!"
Write-Host "  Organiser   : ahmad@paceevents.my   / Password123!"
Write-Host "  Participant : alia@student.uitm.edu.my / Password123!"
Write-Host "  Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""

& $mvn -B compile exec:java
exit $LASTEXITCODE
