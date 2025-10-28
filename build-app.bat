@echo off
setlocal enabledelayedexpansion

echo JavaFX App Builder - Multiple Options
echo =====================================
echo.

:menu
echo Choose build option:
echo 1) Fat JAR (recommended - works everywhere)
echo 2) Try jlink (native image)
echo 3) Standard distribution
echo 4) All options
echo 5) Exit
echo.

:get_choice
set /p choice="Enter choice (1-5): "

if "%choice%"=="" goto invalid_choice
if "%choice%"=="1" goto fatjar
if "%choice%"=="2" goto jlink
if "%choice%"=="3" goto distribution
if "%choice%"=="4" goto all
if "%choice%"=="5" goto exit_script
goto invalid_choice

:invalid_choice
echo.
echo Invalid choice. Please enter 1-5.
echo.
goto get_choice

:fatjar
echo.
echo Building Fat JAR (easiest option)...
call gradlew.bat clean fatJarNoModule
if !errorlevel! equ 0 (
    echo Fat JAR built successfully!
    echo Location: build\libs\quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar
    echo Run with: java -jar "build\libs\quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar"
) else (
    echo Fat JAR build failed with error code !errorlevel!
)
goto end

:jlink
echo.
echo Trying simplified jlink...
call gradlew.bat clean jlink
if !errorlevel! equ 0 (
    echo jlink build successful!
    echo Location: build\image\
    echo Run with: build\image\bin\app.bat
) else (
    echo jlink build failed - use fat JAR instead
)
goto end

:distribution
echo.
echo Building standard distribution...
call gradlew.bat clean distZip distTar
if !errorlevel! equ 0 (
    echo Distribution built successfully!
    echo Location: build\distributions\
    if exist build\distributions\ (
        dir build\distributions\
    )
) else (
    echo Distribution build failed
)
goto end

:all
echo.
echo Building all options...
echo ======================

echo.
echo 1. Building Fat JAR...
call gradlew.bat clean fatJarNoModule
if !errorlevel! equ 0 (
    echo Fat JAR complete
) else (
    echo Fat JAR failed
)

echo.
echo 2. Building Distribution...
call gradlew.bat distZip distTar
if !errorlevel! equ 0 (
    echo Distribution complete
) else (
    echo Distribution failed
)

echo.
echo 3. Building jlink...
call gradlew.bat jlink
if !errorlevel! equ 0 (
    echo jlink complete
) else (
    echo jlink failed
)

goto end

:exit_script
echo.
echo Goodbye!
exit /b 0

:end
echo.
echo Build process completed!
pause