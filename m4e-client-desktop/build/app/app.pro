#-------------------------------------------------
#
# Project created by QtCreator 2017-08-01T12:28:39
#
#-------------------------------------------------

QT += core gui widgets network websockets multimedia

TARGET = Meet4Eat
TEMPLATE = app

DEFINES += QT_DEPRECATED_WARNINGS
CONFIG += c++11
CONFIG += warn_on

INCLUDEPATH += ../../src/app ../../src

SOURCES += \
    ../../src/app/main.cpp \
    ../../src/app/common/basedialog.cpp \
    ../../src/app/common/dialogmessage.cpp \
    ../../src/app/common/guiutils.cpp \
    ../../src/app/communication/connection.cpp \
    ../../src/app/communication/packet.cpp \
    ../../src/app/core/core.cpp \
    ../../src/app/core/log.cpp \
    ../../src/app/core/utils.cpp \
    ../../src/app/core/singleproc.cpp \
    ../../src/app/chat/chatmessage.cpp \
    ../../src/app/chat/chatsystem.cpp \
    ../../src/app/chat/widgetchat.cpp \
    ../../src/app/document/documentcache.cpp \
    ../../src/app/document/modeldocument.cpp \
    ../../src/app/event/dialogbuzz.cpp \
    ../../src/app/event/dialogeventsettings.cpp \
    ../../src/app/event/dialoglocationdetails.cpp \
    ../../src/app/event/dialoglocationedit.cpp \
    ../../src/app/event/events.cpp \
    ../../src/app/event/modelevent.cpp \
    ../../src/app/event/modellocation.cpp \
    ../../src/app/event/modellocationvotes.cpp \
    ../../src/app/event/widgeteventitem.cpp \
    ../../src/app/event/widgeteventlist.cpp \
    ../../src/app/event/widgeteventpanel.cpp \
    ../../src/app/event/widgetlocation.cpp \
    ../../src/app/gui/alarmwindow.cpp \
    ../../src/app/gui/buzzwindow.cpp \
    ../../src/app/gui/mainwindow.cpp \
    ../../src/app/gui/mailboxwindow.cpp \
    ../../src/app/gui/systemtray.cpp \
    ../../src/app/mailbox/mailbox.cpp \
    ../../src/app/mailbox/modelmail.cpp \
    ../../src/app/mailbox/widgetmailedit.cpp \
    ../../src/app/mailbox/widgetmailitem.cpp \
    ../../src/app/mailbox/widgetmaillist.cpp \
    ../../src/app/notification/notifyevent.cpp \
    ../../src/app/notification/notifications.cpp \
    ../../src/app/settings/appsettings.cpp \
    ../../src/app/settings/dialogsettings.cpp \
    ../../src/app/user/dialogsearchuser.cpp \
    ../../src/app/user/dialogusersettings.cpp \
    ../../src/app/user/modeluser.cpp \
    ../../src/app/user/modeluserinfo.cpp \
    ../../src/app/user/user.cpp \
    ../../src/app/user/userauth.cpp \
    ../../src/app/webapp/m4e-api/m4e-response.cpp \
    ../../src/app/webapp/m4e-api/m4e-rest.cpp \
    ../../src/app/webapp/m4e-api/m4e-restops.cpp \
    ../../src/app/webapp/m4e-api/m4e-ws.cpp \
    ../../src/app/webapp/request/rest-authentication.cpp \
    ../../src/app/webapp/request/rest-event.cpp \
    ../../src/app/webapp/request/rest-document.cpp \
    ../../src/app/webapp/request/rest-mailbox.cpp \
    ../../src/app/webapp/request/rest-user.cpp \
    ../../src/app/webapp/response/resp-authentication.cpp \
    ../../src/app/webapp/response/resp-document.cpp \
    ../../src/app/webapp/response/resp-event.cpp \
    ../../src/app/webapp/response/resp-mailbox.cpp \
    ../../src/app/webapp/response/resp-user.cpp \
    ../../src/app/webapp/webapp.cpp \
    ../../src/app/webapp/request/rest-appinfo.cpp \
    ../../src/app/webapp/response/resp-appinfo.cpp

HEADERS += \
    ../../src/app/configuration.h \
    ../../src/app/common/basedialog.h \
    ../../src/app/common/dialogmessage.h \
    ../../src/app/common/guiutils.h \
    ../../src/app/common/modelbase.h \
    ../../src/app/communication/connection.h \
    ../../src/app/communication/packet.h \
    ../../src/app/core/core.h \
    ../../src/app/core/log.h \
    ../../src/app/core/utils.h \
    ../../src/app/core/singleproc.h \
    ../../src/app/core/smartptr.h \
    ../../src/app/core/smartptr.inl \
    ../../src/app/chat/chatmessage.h \
    ../../src/app/chat/chatsystem.h \
    ../../src/app/chat/widgetchat.h \
    ../../src/app/document/documentcache.h \
    ../../src/app/document/modeldocument.h \
    ../../src/app/event/dialogbuzz.h \
    ../../src/app/event/dialoglocationdetails.h \
    ../../src/app/event/dialoglocationedit.h \
    ../../src/app/event/dialogeventsettings.h \
    ../../src/app/event/events.h \
    ../../src/app/event/modelevent.h \
    ../../src/app/event/modellocation.h \
    ../../src/app/event/modellocationvotes.h \
    ../../src/app/event/widgeteventitem.h \
    ../../src/app/event/widgeteventlist.h \
    ../../src/app/event/widgetlocation.h \
    ../../src/app/event/widgeteventpanel.h \
    ../../src/app/gui/alarmwindow.h \
    ../../src/app/gui/buzzwindow.h \
    ../../src/app/gui/mainwindow.h \
    ../../src/app/gui/mailboxwindow.h \
    ../../src/app/gui/systemtray.h \
    ../../src/app/mailbox/mailbox.h \
    ../../src/app/mailbox/modelmail.h \
    ../../src/app/mailbox/widgetmailedit.h \
    ../../src/app/mailbox/widgetmailitem.h \
    ../../src/app/mailbox/widgetmaillist.h \
    ../../src/app/notification/notifyevent.h \
    ../../src/app/notification/notifications.h \
    ../../src/app/settings/appsettings.h \
    ../../src/app/settings/dialogsettings.h \
    ../../src/app/user/dialogusersettings.h \
    ../../src/app/user/dialogsearchuser.h \
    ../../src/app/user/modeluser.h \
    ../../src/app/user/modeluserinfo.h \
    ../../src/app/user/user.h \
    ../../src/app/user/userauth.h \
    ../../src/app/webapp/m4e-api/m4e-response.h \
    ../../src/app/webapp/m4e-api/m4e-rest.h \
    ../../src/app/webapp/m4e-api/m4e-restops.h \
    ../../src/app/webapp/m4e-api/m4e-ws.h \
    ../../src/app/webapp/request/rest-authentication.h \
    ../../src/app/webapp/request/rest-document.h \
    ../../src/app/webapp/request/rest-event.h \
    ../../src/app/webapp/request/rest-mailbox.h \
    ../../src/app/webapp/request/rest-user.h \
    ../../src/app/webapp/response/resp-authentication.h \
    ../../src/app/webapp/response/resp-document.h \
    ../../src/app/webapp/response/resp-event.h \
    ../../src/app/webapp/response/resp-mailbox.h \
    ../../src/app/webapp/response/resp-user.h \
    ../../src/app/webapp/webapp.h \
    ../../src/app/webapp/request/rest-appinfo.h \
    ../../src/app/webapp/response/resp-appinfo.h

FORMS += \
    ../../src/app/gui/forms/mainwindow.ui \
    ../../src/app/gui/forms/basedialog.ui \
    ../../src/app/gui/forms/widgetlocation.ui \
    ../../src/app/gui/forms/widgeteventitem.ui \
    ../../src/app/gui/forms/widgetabout.ui \
    ../../src/app/gui/forms/widgetsettings.ui \
    ../../src/app/gui/forms/widgetlocationdetails.ui \
    ../../src/app/gui/forms/widgeteventsettings.ui \
    ../../src/app/gui/forms/widgetchat.ui \
    ../../src/app/gui/forms/widgeteventpanel.ui \
    ../../src/app/gui/forms/widgetlocationedit.ui \
    ../../src/app/gui/forms/widgetbuzz.ui \
    ../../src/app/gui/forms/mailboxwindow.ui \
    ../../src/app/gui/forms/widgetmailedit.ui \
    ../../src/app/gui/forms/widgetmailitem.ui \
    ../../src/app/gui/forms/widgetsearchuser.ui \
    ../../src/app/gui/forms/dialogalarm.ui \
    ../../src/app/gui/forms/alarmwindow.ui \
    ../../src/app/gui/forms/widgetusersettings.ui \
    ../../src/app/gui/forms/buzzwindow.ui

RESOURCES += \
    ../../src/app/gui/resources/application.qrc

DISTFILES +=

RC_FILE = ../../src/app/gui/resources/app.rc
