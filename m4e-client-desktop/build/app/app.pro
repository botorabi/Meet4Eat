#-------------------------------------------------
#
# Project created by QtCreator 2017-08-01T12:28:39
#
#-------------------------------------------------

QT += core gui network

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = Meet4Eat
TEMPLATE = app

DEFINES += QT_DEPRECATED_WARNINGS
CONFIG += c++11
QMAKE_CXXFLAGS += -Wpedantic -Wextra

debug {
  QMAKE_CXXFLAGS += -O0
}

INCLUDEPATH += ../../src/app ../../src

SOURCES += \
    ../../src/app/main.cpp \
    ../../src/app/core/core.cpp \
    ../../src/app/core/log.cpp \
    ../../src/app/core/utils.cpp \
    ../../src/app/data/appsettings.cpp \
    ../../src/app/data/documentcache.cpp \
    ../../src/app/data/events.cpp \
    ../../src/app/data/modeldocument.cpp \
    ../../src/app/data/modelevent.cpp \
    ../../src/app/data/webapp.cpp \
    ../../src/app/gui/dialogsettings.cpp \
    ../../src/app/gui/guiutils.cpp \
    ../../src/app/gui/mainwindow.cpp \
    ../../src/app/gui/widgeteventitem.cpp \
    ../../src/app/gui/widgetevent.cpp \
    ../../src/app/gui/widgeteventlist.cpp \
    ../../src/app/gui/widgetlocation.cpp \
    ../../src/app/user/userauth.cpp \
    ../../src/app/user/user.cpp \
    ../../src/app/webapp/m4e-api/m4e-response.cpp \
    ../../src/app/webapp/m4e-api/m4e-rest.cpp \
    ../../src/app/webapp/m4e-api/m4e-restops.cpp \
    ../../src/app/webapp/rest-authentication.cpp \
    ../../src/app/webapp/rest-event.cpp \
    ../../src/app/webapp/rest-document.cpp \
    ../../src/app/webapp/rest-user.cpp \
    ../../src/app/webapp/resultshandler/resp-authentication.cpp \
    ../../src/app/webapp/resultshandler/resp-document.cpp \
    ../../src/app/webapp/resultshandler/resp-event.cpp \
    ../../src/app/webapp/resultshandler/resp-user.cpp

HEADERS += \
    ../../src/app/configuration.h \
    ../../src/app/core/core.h \
    ../../src/app/core/log.h \
    ../../src/app/core/utils.h \
    ../../src/app/core/smartptr.h \
    ../../src/app/core/smartptr.inl \
    ../../src/app/data/appsettings.h \
    ../../src/app/data/documentcache.h \
    ../../src/app/data/events.h \
    ../../src/app/data/modelbase.h \
    ../../src/app/data/modeldocument.h \
    ../../src/app/data/modelevent.h \
    ../../src/app/data/modellocation.h \
    ../../src/app/data/modeluser.h \
    ../../src/app/data/webapp.h \
    ../../src/app/gui/mainwindow.h \
    ../../src/app/gui/dialogsettings.h \
    ../../src/app/user/userauth.h \
    ../../src/app/user/user.h \
    ../../src/app/gui/guiutils.h \
    ../../src/app/gui/widgetevent.h \
    ../../src/app/gui/widgeteventitem.h \
    ../../src/app/gui/widgeteventlist.h \
    ../../src/app/gui/widgetlocation.h \
    ../../src/app/webapp/m4e-api/m4e-response.h \
    ../../src/app/webapp/m4e-api/m4e-rest.h \
    ../../src/app/webapp/m4e-api/m4e-restops.h \
    ../../src/app/webapp/resultshandler/resp-authentication.h \
    ../../src/app/webapp/resultshandler/resp-document.h \
    ../../src/app/webapp/resultshandler/resp-event.h \
    ../../src/app/webapp/resultshandler/resp-user.h \
    ../../src/app/webapp/rest-authentication.h \
    ../../src/app/webapp/rest-document.h \
    ../../src/app/webapp/rest-event.h \
    ../../src/app/webapp/rest-user.h

FORMS += \
    ../../src/app/gui/forms/mainwindow.ui \
    ../../src/app/gui/forms/widgetevent.ui \
    ../../src/app/gui/forms/dlgsettings.ui \
    ../../src/app/gui/forms/widgetlocation.ui \
    ../../src/app/gui/forms/widgeteventitem.ui \
    ../../src/app/gui/forms/dlgabout.ui

RESOURCES += \
    ../../src/app/gui/resources/application.qrc
