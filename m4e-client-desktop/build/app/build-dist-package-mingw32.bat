@echo ###################################################
@echo Batch script for building Meet4Eat release
@echo Author: A. Botorabi
@echo Date of Creation: 24th Nov 2017
@echo:
@echo Use 'clean' to remove temporary build directories.
@echo: 
@echo ###################################################

set APP_NAME=Meet4Eat
set PATH_SRC=..\..\src
set PATH_BUILD=..\..\bin\qmake-mingw32-release-batch
set PATH_DEPLOYMENT=..\..\dist\NSIS-MSWin\%APP_NAME%
set PATH_DEPLOYMENT_TEMPLATE=..\..\dist\NSIS-MSWin\mingw32-deployment-template

@echo ##########################
@echo Cleaning build directories
@echo ##########################
rmdir /S /Q %PATH_BUILD%
rmdir /S /Q %PATH_DEPLOYMENT%


@REM for cleaning we are now done
@if "%1"=="clean" goto DONE


@echo #########################
@echo Setting build environment
@echo #########################

@REM Be awre that the path BUILD_PATH below must have the same
@REM  relative path to top dir as the "src" does, otherwise
@REM  the source paths in the pro file won't match!

set CWD=%cd%

@REM following vars can also be set in the shell environmen
@if "%PATH_QT%"=="" set PATH_QT=C:\Qt\5.9.3\mingw53_32\bin
@if "%PATH_MINGW%"=="" set PATH_MINGW=C:\Qt\Tools\mingw530_32\bin
@echo Using PATH_QT=%PATH_QT%
@echo Using PATH_MINGW=%PATH_MINGW%

@if exist %PATH_QT% goto CONT1
@echo ****************************************************
@echo ----------------------------------------------------
@echo ####################################################
@echo *** Wrong path variable PATH_QT, set it up properly.
@echo PATH_QT=%PATH_QT%
@echo ####################################################
@goto DONE
:CONT1

@if exist %PATH_MINGW% goto CONT2
@echo *******************************************************
@echo -------------------------------------------------------
@echo #######################################################
@echo *** Wrong path variable PATH_MINGW, set it up properly.
@echo PATH_MINGW=%PATH_MINGW%
@echo #######################################################
@goto DONE
:CONT2

@REM obviously the mingw compiler needs to be in path!
set PATH=%PATH_MINGW%;%PATH_QT%;%PATH%

@echo ##########################
@echo Creating build directories
@echo ##########################
mkdir %PATH_BUILD%

@echo ###############################
@echo Building %APP_NAME% build files
@echo ###############################
copy app.pro %PATH_BUILD%
cd %PATH_BUILD%

%PATH_QT%\qmake.exe app.pro -r -spec win32-g++

@echo ###################
@echo Building %APP_NAME%
@echo ###################
%PATH_MINGW%\mingw32-make.exe -j4 -f Makefile.Release all

cd %CWD%

@echo ###################################
@echo Building the deployment package ...
@echo ###################################

rmdir /S /Q %PATH_DEPLOYMENT%
mkdir %PATH_DEPLOYMENT%
copy %PATH_BUILD%\release\%APP_NAME%.exe %PATH_DEPLOYMENT%\
cd %PATH_DEPLOYMENT%
%PATH_QT%\windeployqt.exe %APP_NAME%.exe

cd %CWD%
copy %PATH_DEPLOYMENT%\..\extra-libs\* %PATH_DEPLOYMENT%\
copy %PATH_DEPLOYMENT%\..\app.ico %PATH_DEPLOYMENT%\
copy %PATH_DEPLOYMENT%\..\LICENSE %PATH_DEPLOYMENT%\

@cd %PATH_DEPLOYMENT%
@echo #######################################################
@echo Distribution package successfully built into directory:
@echo  %cd%
@echo #######################################################
@cd %CWD%

@echo ######################################
@echo Building the application installer ...
@echo ######################################
@cd %PATH_DEPLOYMENT%\..\msw-installer
cmd /C .\make-installer.bat
@cd %PATH_DEPLOYMENT%\..
@echo ###############################################
@echo Application installer was created in directory:
@echo %cd%
@echo ###############################################

@cd %CWD%

:DONE
pause
