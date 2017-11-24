@echo off
set SRC_FILE=..\..\..\src\app\configuration.h

for /f "tokens=*" %%i in ('findstr /r  ".*define.*M4E_APP_VERSION.*$" %SRC_FILE%') do set ver=%%i
for /f "tokens=3" %%a in ("%ver%") do set version=%%a
set version=%version:"=%
echo %version%