@REM Maven Wrapper startup batch script for api-gateway
@REM ------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM If wrapper jar doesn't exist, download it
if not exist %WRAPPER_JAR% (
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"
)

@REM Find java.exe
if defined JAVA_HOME goto useJavaHome

@REM Try IntelliJ bundled JDK
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\jbr"
    goto execute
)

echo ERROR: JAVA_HOME is not set and no JDK found.
exit /b 1

:useJavaHome
:execute
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" ^
  -jar %WRAPPER_JAR% ^
  %*

endlocal
