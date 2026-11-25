@echo off
setlocal
cd /d "%~dp0"
set "JAVA=java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if "%JAVA%"=="java.exe" if exist "%USERPROFILE%\.jdks\ms-17.0.20.1\bin\java.exe" set "JAVA=%USERPROFILE%\.jdks\ms-17.0.20.1\bin\java.exe"
set "JAR=facturation-service\target\facturation-service-1.0.0.jar"
if not exist "%JAR%" (
  echo Fichier introuvable : %JAR%
  echo Compiler avec mvn clean package -DskipTests.
  exit /b 1
)
"%JAVA%" -jar "%JAR%"
exit /b %errorlevel%
