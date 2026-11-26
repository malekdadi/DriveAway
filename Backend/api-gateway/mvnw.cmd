@REM Maven Wrapper startup batch script for api-gateway
@REM ------------------------------------------------------------------
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    if errorlevel 1 (
        echo ERROR: Failed to download maven-wrapper.jar from %WRAPPER_URL%
        exit /b 1
    )
)

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" goto execute
)

for /f "delims=" %%i in ('where java 2^>nul') do (
    set "JAVA_EXE=%%i"
    for %%j in ("%%~dpi..") do set "JAVA_HOME=%%~fj"
    goto execute
)

if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\jbr"
    goto execute
)

echo ERROR: JAVA_HOME is not set and java was not found on PATH.
exit /b 1

:execute
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*

endlocal
