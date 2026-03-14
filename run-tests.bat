@echo off
echo ======================================================
echo  Sims CLI Game — JUnit 5 Test Runner
echo ======================================================

echo.
echo [1/2] Compiling source code...
javac -d bin -sourcepath src src/simcli/Main.java
if %errorlevel% neq 0 (
    echo [ERROR] Source compilation failed. Fix compile errors first.
    exit /b 1
)

echo [2/2] Compiling tests...
if not exist bin\test mkdir bin\test
javac -cp "lib\junit-platform-console-standalone.jar;bin" -d bin\test -sourcepath test test\simcli\ReproduceTest.java test\simcli\LifecycleTest.java test\simcli\TimeManagerTest.java
if %errorlevel% neq 0 (
    echo [ERROR] Test compilation failed.
    exit /b 1
)

echo.
echo Running tests...
echo ======================================================
java -jar lib\junit-platform-console-standalone.jar --class-path "bin;bin\test" --scan-class-path --include-package simcli --disable-ansi-colors
