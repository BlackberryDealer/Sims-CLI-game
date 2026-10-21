@echo off
rem ------------------------------------------------------------------
rem  Locates a Java Development Kit and sets JAVAC and JAVA for the
rem  calling script. Used by run.bat and run-tests.bat.
rem
rem  Search order:
rem    1. %JAVA_HOME%
rem    2. javac on the PATH
rem    3. The JDK bundled with VS Code's "Language Support for Java" extension
rem    4. Common install folders (Oracle, Adoptium/Temurin, Microsoft, Zulu, Corretto)
rem ------------------------------------------------------------------
set "JDK_BIN="

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JDK_BIN=%JAVA_HOME%\bin"

if not defined JDK_BIN (
    for /f "delims=" %%P in ('where javac 2^>nul') do if not defined JDK_BIN set "JDK_BIN=%%~dpP"
)

if not defined JDK_BIN (
    for /d %%E in ("%USERPROFILE%\.vscode\extensions\redhat.java-*") do (
        for /d %%J in ("%%E\jre\*") do (
            if exist "%%J\bin\javac.exe" set "JDK_BIN=%%J\bin"
        )
    )
)

if not defined JDK_BIN (
    for %%R in ("%ProgramFiles%\Java" "%ProgramFiles%\Eclipse Adoptium" "%ProgramFiles%\Microsoft" "%ProgramFiles%\Zulu" "%ProgramFiles%\Amazon Corretto") do (
        for /d %%J in ("%%~R\jdk*") do (
            if exist "%%J\bin\javac.exe" set "JDK_BIN=%%J\bin"
        )
    )
)

if not defined JDK_BIN (
    echo.
    echo [ERROR] Could not find a Java Development Kit ^(javac^).
    echo         Install one, for example with:
    echo             winget install EclipseAdoptium.Temurin.21.JDK
    echo         or set JAVA_HOME to an existing JDK folder, then try again.
    exit /b 1
)

if "%JDK_BIN:~-1%"=="\" set "JDK_BIN=%JDK_BIN:~0,-1%"
set "JAVAC=%JDK_BIN%\javac.exe"
set "JAVA=%JDK_BIN%\java.exe"
echo Using JDK: %JDK_BIN%
exit /b 0
