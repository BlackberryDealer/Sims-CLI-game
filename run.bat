@echo off
setlocal
cd /d "%~dp0"

call "%~dp0find-jdk.bat"
if errorlevel 1 (
    pause
    exit /b 1
)

echo Compiling game...
"%JAVAC%" -encoding UTF-8 -d bin -sourcepath src src/simcli/Main.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Starting game...
rem Switch the console to UTF-8 so box lines, bars, stars and hearts display properly.
chcp 65001 >nul
"%JAVA%" -cp "bin;resources" simcli.Main
pause
