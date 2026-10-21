@echo off
setlocal
cd /d "%~dp0"
echo ======================================================
echo  Sims CLI Game - JUnit 5 Test Runner
echo ======================================================

call "%~dp0find-jdk.bat"
if errorlevel 1 exit /b 1

echo.
echo [1/2] Compiling source code...
dir /s /B src\*.java > src_sources.txt
"%JAVAC%" -encoding UTF-8 -d bin @src_sources.txt
set "RESULT=%errorlevel%"
del src_sources.txt
if not "%RESULT%"=="0" (
    echo [ERROR] Source compilation failed. Fix compile errors first.
    exit /b 1
)

echo [2/2] Compiling tests...
if not exist bin\test mkdir bin\test
dir /s /B test\*.java > test_sources.txt
"%JAVAC%" -encoding UTF-8 -cp "lib\junit-platform-console-standalone.jar;bin" -d bin\test -sourcepath test @test_sources.txt
set "RESULT=%errorlevel%"
del test_sources.txt
if not "%RESULT%"=="0" (
    echo [ERROR] Test compilation failed.
    exit /b 1
)

echo.
echo Running tests...
echo ======================================================
"%JAVA%" -jar lib\junit-platform-console-standalone.jar execute --class-path "bin;bin\test" --scan-class-path --include-package simcli --disable-ansi-colors
