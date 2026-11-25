@echo off
setlocal
cd /d "%~dp0"
set "JAVA=java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if "%JAVA%"=="java.exe" if exist "%USERPROFILE%\.jdks\ms-17.0.20.1\bin\java.exe" set "JAVA=%USERPROFILE%\.jdks\ms-17.0.20.1\bin\java.exe"
"%JAVA%" -version
if errorlevel 1 goto failed
powershell.exe -NoProfile -Command "$ports = 8761,8083,8080; $busy = @(Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object LocalPort -In $ports | Select-Object -ExpandProperty LocalPort -Unique); if ($busy.Count) { Write-Host ('Ports deja utilises : ' + ($busy -join ', ')); exit 1 }; if (!(Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue)) { Write-Host 'Demarrer MySQL dans XAMPP avant de continuer.'; exit 1 }"
if errorlevel 1 goto failed
set "EUREKA_PORT=8761"
set "PORT=8083"
set "GATEWAY_PORT=8080"
set "EUREKA_SERVER_URL=http://127.0.0.1:8761/eureka/"
start "DriveAway - Eureka 8761" "%JAVA%" -jar "eureka-server\target\eureka-server-1.0.0.jar"
call :wait_http http://127.0.0.1:8761/
if errorlevel 1 goto failed
start "DriveAway - Paiement 8083" "%JAVA%" -jar "paiement-service\target\paiement-service-1.0.0.jar"
call :wait_http http://127.0.0.1:8761/eureka/apps/PAIEMENT-SERVICE
if errorlevel 1 goto failed
start "DriveAway - Gateway 8080" "%JAVA%" -jar "api-gateway\target\api-gateway-1.0.0.jar"
call :wait_http http://127.0.0.1:8080/api/factures
if errorlevel 1 goto failed
echo Dashboard Eureka  http://127.0.0.1:8761/
echo Factures          http://127.0.0.1:8080/api/factures
pause
exit /b 0

:wait_http
echo Verification de %~1
powershell.exe -NoProfile -Command "$end = (Get-Date).AddSeconds(120); do { try { if ((Invoke-WebRequest '%~1' -UseBasicParsing -TimeoutSec 3).StatusCode -eq 200) { exit 0 } } catch {}; Start-Sleep -Seconds 2 } while ((Get-Date) -lt $end); exit 1"
exit /b %errorlevel%

:failed
echo Demarrage interrompu. Consulter les fenetres Spring Boot ou les messages ci-dessus.
pause
exit /b 1
