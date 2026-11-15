#requires -Version 5.1
<#
.SYNOPSIS
    End-to-end smoke test for a running RunEase instance.

.DESCRIPTION
    Signs in as each role, checks the main pages, verifies role isolation and
    CSRF protection, and calls the admin JSON API. Prints PASS/FAIL per check.

.EXAMPLE
    .\scripts\smoke-test.ps1

.EXAMPLE
    .\scripts\smoke-test.ps1 -BaseUrl http://localhost:9090
#>
[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$script:passed = 0
$script:failed = 0

function Get-Page {
    param($Session, [string]$Path)
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl$Path" -WebSession $Session `
            -UseBasicParsing -MaximumRedirection 5 -TimeoutSec 30
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Content = [string]$response.Content }
    } catch {
        $response = $_.Exception.Response
        if ($null -ne $response) {
            return [pscustomobject]@{ Status = [int]$response.StatusCode; Content = "" }
        }
        return [pscustomobject]@{ Status = 0; Content = $_.Exception.Message }
    }
}

function Check {
    param([string]$Name, [int]$Actual, [int[]]$Expected)
    if ($Expected -contains $Actual) {
        $script:passed++
        Write-Host ("  PASS  {0,-52} {1}" -f $Name, $Actual) -ForegroundColor Green
    } else {
        $script:failed++
        Write-Host ("  FAIL  {0,-52} {1} (expected {2})" -f $Name, $Actual, ($Expected -join "/")) -ForegroundColor Red
    }
}

function Get-Redirect {
    param($Session, [string]$Path)
    $request = [System.Net.HttpWebRequest]::Create("$BaseUrl$Path")
    $request.Method = "GET"
    $request.AllowAutoRedirect = $false
    $request.CookieContainer = $Session.Cookies
    $request.Timeout = 30000
    try {
        $response = $request.GetResponse()
    } catch [System.Net.WebException] {
        $response = $_.Exception.Response
    }
    if ($null -eq $response) {
        return [pscustomobject]@{ Status = 0; Location = "" }
    }
    try {
        return [pscustomobject]@{
            Status   = [int]$response.StatusCode
            Location = [string]$response.Headers["Location"]
        }
    } finally {
        $response.Close()
    }
}

function Check-Redirect {
    param([string]$Name, $Response, [string]$ExpectedPath)
    $isRedirect = $Response.Status -in 301, 302, 303, 307, 308
    if ($isRedirect -and $Response.Location -match [regex]::Escape($ExpectedPath)) {
        $script:passed++
        Write-Host ("  PASS  {0,-52} {1} -> {2}" -f $Name, $Response.Status, $ExpectedPath) -ForegroundColor Green
    } else {
        $script:failed++
        Write-Host ("  FAIL  {0,-52} {1} -> {2} (expected {3})" -f $Name, $Response.Status, $Response.Location, $ExpectedPath) -ForegroundColor Red
    }
}

function New-Session {
    return New-Object Microsoft.PowerShell.Commands.WebRequestSession
}

function Get-Csrf {
    param($Session)
    $page = Get-Page -Session $Session -Path "/login"
    $match = [regex]::Match($page.Content, 'name="csrf-token" content="([^"]+)"')
    if (-not $match.Success) { throw "CSRF token not found on /login" }
    return $match.Groups[1].Value
}

function Connect-Role {
    param([string]$Email)
    $session = New-Session
    $token = Get-Csrf -Session $session
    try {
        Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession $session -UseBasicParsing -MaximumRedirection 5 `
            -Method Post -Body @{ _csrf = $token; email = $Email; password = "Password123!" } -TimeoutSec 30 | Out-Null
    } catch {
        throw "Login failed for $Email : $($_.Exception.Message)"
    }
    return $session
}

Write-Host ""
Write-Host "RunEase smoke test - $BaseUrl" -ForegroundColor Cyan
Write-Host ""

# --- Wait for the server ----------------------------------------------------
$ready = $false
for ($i = 0; $i -lt 40; $i++) {
    $probe = Get-Page -Session (New-Session) -Path "/"
    if ($probe.Status -eq 200) { $ready = $true; break }
    Start-Sleep -Milliseconds 1500
}
if (-not $ready) {
    Write-Host "Server is not responding at $BaseUrl. Start it with .\run.ps1 first." -ForegroundColor Red
    exit 1
}

# --- Public pages -----------------------------------------------------------
Write-Host "Public area"
$public = New-Session
Check "GET /" (Get-Page $public "/").Status 200
Check "GET /events" (Get-Page $public "/events").Status 200
Check "GET /events/2" (Get-Page $public "/events/2").Status 200
Check "GET /login" (Get-Page $public "/login").Status 200
Check "GET /register" (Get-Page $public "/register").Status 200
Check-Redirect "GET /dashboard (guest -> login)" (Get-Redirect $public "/dashboard") "/login"

# --- Participant ------------------------------------------------------------
Write-Host "Participant"
$participant = Connect-Role "alia@student.uitm.edu.my"
Check "GET /dashboard" (Get-Page $participant "/dashboard").Status 200
Check "GET /registrations" (Get-Page $participant "/registrations").Status 200
Check "GET /profile" (Get-Page $participant "/profile").Status 200
Check "GET /admin (blocked)" (Get-Page $participant "/admin").Status 403
Check "GET /organizer (blocked)" (Get-Page $participant "/organizer").Status 403

# --- Organiser --------------------------------------------------------------
Write-Host "Organiser"
$organizer = Connect-Role "ahmad@paceevents.my"
Check "GET /organizer" (Get-Page $organizer "/organizer").Status 200
Check "GET /organizer/events" (Get-Page $organizer "/organizer/events").Status 200
Check "GET /organizer/participants" (Get-Page $organizer "/organizer/participants").Status 200
Check "GET /organizer/results" (Get-Page $organizer "/organizer/results").Status 200
Check "GET /organizer/reports" (Get-Page $organizer "/organizer/reports").Status 200
Check "GET /admin (blocked)" (Get-Page $organizer "/admin").Status 403

# --- Administrator ----------------------------------------------------------
Write-Host "Administrator"
$admin = Connect-Role "admin@runease.my"
Check "GET /admin" (Get-Page $admin "/admin").Status 200
Check "GET /admin/events" (Get-Page $admin "/admin/events").Status 200
Check "GET /admin/organizers" (Get-Page $admin "/admin/organizers").Status 200
Check "GET /admin/users" (Get-Page $admin "/admin/users").Status 200
Check "GET /admin/audit" (Get-Page $admin "/admin/audit").Status 200
$api = Get-Page $admin "/api/admin/users?page=1&size=5"
Check "GET /api/admin/users (JSON)" $api.Status 200
if ($api.Status -eq 200 -and $api.Content -notmatch '"success"') {
    Write-Host "  INFO  API response does not look like the expected JSON envelope" -ForegroundColor Yellow
}

# --- Security ---------------------------------------------------------------
Write-Host "Security"
try {
    Invoke-WebRequest -Uri "$BaseUrl/logout" -WebSession $admin -UseBasicParsing `
        -Method Post -Body @{ } -MaximumRedirection 0 -TimeoutSec 30 | Out-Null
    Check "POST /logout without CSRF" 200 403
} catch {
    $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
    Check "POST /logout without CSRF" $code 403
}
Check "GET /api/admin/users (guest)" (Get-Page (New-Session) "/api/admin/users").Status 401

Write-Host ""
if ($script:failed -eq 0) {
    Write-Host "All $script:passed checks passed." -ForegroundColor Green
    exit 0
}
Write-Host "$script:failed check(s) failed, $script:passed passed." -ForegroundColor Red
exit 1
