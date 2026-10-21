@echo off
echo Compiling game...
javac -encoding UTF-8 -d bin -sourcepath src src/simcli/Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Starting game...
rem Switch the console to UTF-8 so box lines, bars, stars and hearts display properly.
chcp 65001 >nul
java -cp bin simcli.Main
pause
