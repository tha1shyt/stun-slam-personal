@echo off
setlocal enabledelayedexpansion
title Stun Slam Mod Builder

echo ===================================================
echo         ⚡ Stun Slam Mod - 1-Click Builder
echo ===================================================
echo.

:: Check for JDK 21
echo [1/3] Checking for Java JDK 21...
set JDK_FOUND=0

where javac >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] JDK compiler found on system PATH!
    set JDK_FOUND=1
) else (
    :: Check common JDK 21 paths
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\javac.exe" (
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%D"
        echo [OK] Found JDK 21 at: !JAVA_HOME!
        set JDK_FOUND=1
    ) else if exist "C:\Program Files\Java\jdk-21*\bin\javac.exe" (
        for /d %%D in ("C:\Program Files\Java\jdk-21*") do set "JAVA_HOME=%%D"
        echo [OK] Found JDK 21 at: !JAVA_HOME!
        set JDK_FOUND=1
    ) else if exist "C:\Program Files\Microsoft\jdk-21*\bin\javac.exe" (
        for /d %%D in ("C:\Program Files\Microsoft\jdk-21*") do set "JAVA_HOME=%%D"
        echo [OK] Found JDK 21 at: !JAVA_HOME!
        set JDK_FOUND=1
    )
)

if %JDK_FOUND% equ 0 (
    echo.
    echo [X] Java JDK 21 was not found on your PC.
    echo.
    echo Minecraft requires JDK 21 to compile mods.
    echo Would you like to automatically install JDK 21 via winget? (Y/N)
    set /p INSTALL_JDK="> "
    if /i "!INSTALL_JDK!"=="Y" (
        echo Installing Eclipse Temurin JDK 21...
        winget install EclipseAdoptium.Temurin.21.jdk --accept-source-agreements --accept-package-agreements
        echo.
        echo Please restart this script after the installer completes!
        pause
        exit /b
    ) else (
        echo You can manually download JDK 21 from: https://adoptium.net/temurin/releases/?version=21
        pause
        exit /b
    )
)

:: Build the mod using Gradle
echo.
echo [2/3] Compiling Stun Slam into a .jar with Gradle...
echo (First-time build will download Fabric libraries, please wait a minute)
echo.

call gradlew.bat build --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [X] Build encountered an error. Check the logs above.
    pause
    exit /b
)

:: Find the output jar and copy to .minecraft/mods
echo.
echo [3/3] Locating compiled jar...

for %%F in (build\libs\StunSlam-*.jar) do (
    set "JAR_FILE=%%~fF"
    set "JAR_NAME=%%~nxF"
)

if not defined JAR_FILE (
    echo [X] Could not find the generated .jar in build\libs\
    pause
    exit /b
)

echo [OK] Mod compiled successfully: !JAR_NAME!
echo.
echo Would you like to automatically copy !JAR_NAME! into your .minecraft/mods folder? (Y/N)
set /p COPY_MOD="> "
if /i "!COPY_MOD!"=="Y" (
    if not exist "%APPDATA%\.minecraft\mods" mkdir "%APPDATA%\.minecraft\mods"
    copy "!JAR_FILE!" "%APPDATA%\.minecraft\mods\!JAR_NAME!"
    echo.
    echo [SUCCESS] !JAR_NAME! copied to %APPDATA%\.minecraft\mods\
    echo You can now launch Minecraft Fabric 1.21.4 and play!
)

echo.
echo All done! Press any key to exit.
pause >nul
