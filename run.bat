@echo off
echo Compiling game...
javac -d bin -sourcepath src src/simcli/Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Starting game...
java -cp bin simcli.Main
pause
