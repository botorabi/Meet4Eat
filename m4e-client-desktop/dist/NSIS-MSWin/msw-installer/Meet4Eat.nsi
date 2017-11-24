;--------------------------------
; Meet4Eat installer
; Created: 24th Nov 2017
; Author: boto
;--------------------------------

; NOTE: pass the version string using /DVERSION="x.x.x" parameter of makensis.exe
; NOTE: pass the language string using /DLANGUAGE=<"German" | "English"> parameter of makensis.exe

;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  !define PROGRAM_NAME      "Meet4Eat"
  !define COMPANY_NAME      "VR Fun"  
  !define INSTALLER_NAME 	"Meet4Eat"
  !define SRC_FOLDER     	..\Meet4Eat
  
  !define MUI_ICON          "app.ico"
  !define MUI_UNICON        "app.ico"
  !define MUI_WELCOMEFINISHPAGE_BITMAP_NOSTRETCH
  !define MUI_WELCOMEFINISHPAGE_BITMAP "install.bmp"
  
  ;Name and file
  Name "${INSTALLER_NAME}-v${VERSION}"
  OutFile "..\${INSTALLER_NAME}-Setup-v${VERSION}.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\${INSTALLER_NAME}"

  ;Get installation folder from registry if available
  InstallDirRegKey HKLM "Software\${INSTALLER_NAME}" ""

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "${SRC_FOLDER}\LICENSE"
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !define MUI_FINISHPAGE_NOAUTOCLOSE
  !define MUI_FINISHPAGE_RUN
  !define MUI_FINISHPAGE_RUN_CHECKED
  !define MUI_FINISHPAGE_RUN_TEXT "Starte ${PROGRAM_NAME}"
  !define MUI_FINISHPAGE_RUN_FUNCTION "LaunchProgram"
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE ${LANGUAGE}

  ;LoadLanguageFile "${NSISDIR}\Contrib\Language Files\English.nlf"
  VIProductVersion  "${VERSION}.0"
  ;VIAddVersionKey  /LANG=${LANG_ENGLISH} "Comments" ""
  VIAddVersionKey   "ProductName" "${PROGRAM_NAME}"
  VIAddVersionKey   "CompanyName" "${COMPANY_NAME}"
  VIAddVersionKey   "LegalCopyright" "Copyright 2017, All rights reserved"
  VIAddVersionKey   "FileDescription" "${PROGRAM_NAME} Installer"
  VIAddVersionKey   "FileVersion" ${VERSION}
  VIAddVersionKey   "ProductVersion" ${VERSION}
  VIAddVersionKey   "InternalName" "${INSTALLER_NAME}-v${VERSION}"
  VIAddVersionKey   "LegalTrademarks" "${PROGRAM_NAME} is a Trademark of ${COMPANY_NAME}"
  VIAddVersionKey   "OriginalFilename" "${INSTALLER_NAME}-Setup-v${VERSION}-${LANGUAGE}.exe"

;--------------------------------
;Installer Sections

Section "Main" SecMain

  SetOutPath "$INSTDIR"

  ; copy all files
  File /r ${SRC_FOLDER}\*

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayName" "${PROGRAM_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayIcon" "$\"$INSTDIR\app.ico$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "Publisher" "${COMPANY_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayVersion" "${VERSION}"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  WriteRegStr HKLM "Software\${INSTALLER_NAME}" "" $INSTDIR

  ;create start menu entries
  CreateDirectory "$SMPROGRAMS\${PROGRAM_NAME}"
  CreateShortCut "$SMPROGRAMS\${PROGRAM_NAME}\${PROGRAM_NAME}.lnk" "$INSTDIR\${PROGRAM_NAME}.exe"
  CreateShortCut "$SMPROGRAMS\${PROGRAM_NAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0

  ;create desktop shortcut
  CreateShortCut "$DESKTOP\${PROGRAM_NAME}.lnk" "$INSTDIR\${PROGRAM_NAME}.exe" ""

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecMain ${LANG_ENGLISH} "Main"

  ;Assign language strings to sections
  ;!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  ;!insertmacro MUI_DESCRIPTION_TEXT ${SecMain} $(DESC_SecMain)
  ;!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"

  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\${PROGRAM_NAME}"
  Delete "$DESKTOP\${PROGRAM_NAME}.lnk"

  DeleteRegKey HKLM "Software\${INSTALLER_NAME}"

  ;remove app's registry key
  DeleteRegKey HKCU "Software\${COMPANY_NAME}\${INSTALLER_NAME}"
  DeleteRegKey /ifempty HKCU "Software\${COMPANY_NAME}"

  ;remove Uninstaller And Unistall Registry Entries
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}"  

SectionEnd

Function LaunchProgram
  ExecShell "" "$INSTDIR\${PROGRAM_NAME}.exe"
FunctionEnd