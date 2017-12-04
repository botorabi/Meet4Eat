@REM build the Meet4Eat installer

@if exist nsis goto CONT
.\unzip nsis.zip
:CONT

@for /f "tokens=*" %%i in ('get-app-version.bat') do set VERSION=%%i

@echo ############################
@echo Building the installer...
@echo ############################
nsis\makensis.exe /DVERSION=%VERSION% Meet4Eat.nsi

@echo ##################################
@echo Installer was created successfully
@echo ##################################
