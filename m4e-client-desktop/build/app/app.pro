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
    ../../src/app/common/basedialog.cpp \
    ../../src/app/common/dialogmessage.cpp \
    ../../src/app/common/guiutils.cpp \
    ../../src/app/core/core.cpp \
    ../../src/app/core/log.cpp \
    ../../src/app/core/utils.cpp \
    ../../src/app/document/documentcache.cpp \
    ../../src/app/document/modeldocument.cpp \
    ../../src/app/event/dialogeventsettings.cpp \
    ../../src/app/event/dialoglocationdetails.cpp \
    ../../src/app/event/events.cpp \
    ../../src/app/event/modelevent.cpp \
    ../../src/app/event/widgetevent.cpp \
    ../../src/app/event/widgeteventitem.cpp \
    ../../src/app/event/widgeteventlist.cpp \
    ../../src/app/event/widgetlocation.cpp \
    ../../src/app/gui/mainwindow.cpp \
    ../../src/app/settings/appsettings.cpp \
    ../../src/app/settings/dialogsettings.cpp \
    ../../src/app/user/user.cpp \
    ../../src/app/user/userauth.cpp \
    ../../src/app/webapp/m4e-api/m4e-response.cpp \
    ../../src/app/webapp/m4e-api/m4e-rest.cpp \
    ../../src/app/webapp/m4e-api/m4e-restops.cpp \
    ../../src/app/webapp/request/rest-authentication.cpp \
    ../../src/app/webapp/request/rest-event.cpp \
    ../../src/app/webapp/request/rest-document.cpp \
    ../../src/app/webapp/request/rest-user.cpp \
    ../../src/app/webapp/response/resp-authentication.cpp \
    ../../src/app/webapp/response/resp-document.cpp \
    ../../src/app/webapp/response/resp-event.cpp \
    ../../src/app/webapp/response/resp-user.cpp \
    ../../src/app/webapp/webapp.cpp

HEADERS += \
    ../../src/app/configuration.h \
    ../../src/app/common/basedialog.h \
    ../../src/app/common/dialogmessage.h \
    ../../src/app/common/guiutils.h \
    ../../src/app/common/modelbase.h \
    ../../src/app/core/core.h \
    ../../src/app/core/log.h \
    ../../src/app/core/utils.h \
    ../../src/app/core/smartptr.h \
    ../../src/app/core/smartptr.inl \
    ../../src/app/document/documentcache.h \
    ../../src/app/document/modeldocument.h \
    ../../src/app/event/dialoglocationdetails.h \
    ../../src/app/event/dialogeventsettings.h \
    ../../src/app/event/events.h \
    ../../src/app/event/modelevent.h \
    ../../src/app/event/modellocation.h \
    ../../src/app/event/widgetevent.h \
    ../../src/app/event/widgeteventitem.h \
    ../../src/app/event/widgeteventlist.h \
    ../../src/app/event/widgetlocation.h \
    ../../src/app/gui/mainwindow.h \
    ../../src/app/settings/appsettings.h \
    ../../src/app/settings/dialogsettings.h \
    ../../src/app/user/modeluser.h \
    ../../src/app/user/modeluserinfo.h \
    ../../src/app/user/user.h \
    ../../src/app/user/userauth.h \
    ../../src/app/webapp/m4e-api/m4e-response.h \
    ../../src/app/webapp/m4e-api/m4e-rest.h \
    ../../src/app/webapp/m4e-api/m4e-restops.h \
    ../../src/app/webapp/request/rest-authentication.h \
    ../../src/app/webapp/request/rest-document.h \
    ../../src/app/webapp/request/rest-event.h \
    ../../src/app/webapp/request/rest-user.h \
    ../../src/app/webapp/response/resp-authentication.h \
    ../../src/app/webapp/response/resp-document.h \
    ../../src/app/webapp/response/resp-event.h \
    ../../src/app/webapp/response/resp-user.h \
    ../../src/app/webapp/webapp.h

FORMS += \
    ../../src/app/gui/forms/mainwindow.ui \
    ../../src/app/gui/forms/widgetevent.ui \
    ../../src/app/gui/forms/widgetlocation.ui \
    ../../src/app/gui/forms/widgeteventitem.ui \
    ../../src/app/gui/forms/basedialog.ui \
    ../../src/app/gui/forms/widgetabout.ui \
    ../../src/app/gui/forms/widgetsettings.ui \
    ../../src/app/gui/forms/widgetlocationdetails.ui \
    ../../src/app/gui/forms/widgeteventsettings.ui

RESOURCES += \
    ../../src/app/gui/resources/application.qrc
