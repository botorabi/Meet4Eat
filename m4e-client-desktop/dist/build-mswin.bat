@echo ###################################################
@echo Batch script for building Meet4Eat release
@echo Author: A. Botorabi
@echo Date of Creation: 24th Nov 2017-2018
@echo:
@echo Use 'clean' to remove temporary build directories.
@echo: 
@echo ###################################################
@set DIR_INSTALL=Installers
@echo Setup the build tools paths
@set PATH_QT=C:\Qt\5.9.3\mingw53_32\bin
@set PATH_MINGW=C:\Qt\Tools\mingw530_32\bin
@echo Using PATH_QT=%PATH_QT%
@echo Using PATH_MINGW=%PATH_MINGW%

cd NSIS-MSWin
call build-dist-package-mingw32.bat %*
move /Y Meet4Eat-mswin-v*.exe ..\%DIR_INSTALL%\
call build-dist-package-mingw32.bat clean
cd ..
@echo Installer was moved to directory: %DIR_INSTALL%
pause
