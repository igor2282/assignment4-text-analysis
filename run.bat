@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set TARGET=%1
if "%TARGET%"=="" set TARGET=all

if exist out rmdir /s /q out
mkdir out
set ROOT=%CD%\
(for /r src %%f in (*.java) do (
    set F=%%f
    set F=!F:%ROOT%=!
    echo !F:\=/!
)) > sources.txt
javac --release 17 -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    exit /b 1
)
del sources.txt

if "%TARGET%"=="build" goto :eof
if "%TARGET%"=="test" (
    java -cp out textanalysis.TestRunner
    goto :eof
)
if "%TARGET%"=="demo" (
    java -cp out textanalysis.Main
    goto :eof
)
if "%TARGET%"=="bench" (
    java -Xms512m -cp out textanalysis.bench.Benchmark results
    goto :eof
)
if "%TARGET%"=="plots" (
    python scripts\plot_results.py
    goto :eof
)

java -cp out textanalysis.TestRunner
java -cp out textanalysis.Main
java -Xms512m -cp out textanalysis.bench.Benchmark results
python scripts\plot_results.py
