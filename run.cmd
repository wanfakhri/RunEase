@echo off
setlocal
cd /d "%~dp0"

rem --- Locate a JDK ---------------------------------------------------------
if not "%JAVA_HOME%"=="" goto java_ok
for /f "delims=" %%j in ('where javac 2^>nul') do (
    for %%d in ("%%~dpj..") do set "JAVA_HOME=%%~fd"
)
if "%JAVA_HOME%"=="" (
    echo.
    echo Java Development Kit ^(JDK 21^) was not found.
    echo Install a JDK and set JAVA_HOME, then run this script again.
    echo.
    exit /b 1
)
:java_ok

rem --- Locate Maven ---------------------------------------------------------
if exist "%~dp0mvnw.cmd" (
    set "MVN=%~dp0mvnw.cmd"
) else (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo.
        echo Maven was not found.
        echo Install Maven 3.9+ or keep the bundled mvnw.cmd wrapper in this folder.
        echo.
        exit /b 1
    )
    set "MVN=mvn"
)

if "%PORT%"=="" set "PORT=8080"

echo.
echo   RunEase - Running Event Management System
echo   URL         : http://localhost:%PORT%/
echo   Admin       : admin@runease.my        / Password123!
echo   Organiser   : ahmad@paceevents.my     / Password123!
echo   Participant : alia@student.uitm.edu.my / Password123!
echo   Press Ctrl+C to stop.
echo.

call "%MVN%" -B compile exec:java
exit /b %ERRORLEVEL%
