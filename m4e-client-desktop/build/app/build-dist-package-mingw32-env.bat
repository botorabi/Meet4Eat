@echo ###################################################
@echo Batch script for building Meet4Eat release
@echo Author: A. Botorabi
@echo Date of Creation: 24th Nov 2017
@echo:
@echo Use 'clean' to remove temporary build directories.
@echo: 
@echo ###################################################
@echo Setup the build tools paths
@set PATH_QT=D:\Dev\Qt\5.9.3\mingw53_32\bin
@set PATH_MINGW=D:\Dev\Qt\Tools\mingw530_32\bin
@echo Using PATH_QT=%PATH_QT%
@echo Using PATH_MINGW=%PATH_MINGW%

.\build-dist-package-mingw32.bat %*

pause