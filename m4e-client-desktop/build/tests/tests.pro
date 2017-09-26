#-------------------------------------------------
#
# Project created by QtCreator 2017-08-01T12:28:39
#
#-------------------------------------------------

QT += core gui testlib

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = Meet4Eat
TEMPLATE = app
DEFINES += QT_DEPRECATED_WARNINGS
CONFIG += c++11
QMAKE_CXXFLAGS += -Wpedantic -Wextra

INCLUDEPATH += ../../tests ../../src/app ../../src

SOURCES += \
    ../../tests/main.cpp \
    ../../src/app/core/log.cpp \
    ../../src/app/core/utils.cpp \
    ../../src/app/data/events.cpp \
    ../../src/app/data/modelevent.cpp \
    ../../tests/testevents.cpp

HEADERS += \
    ../../src/app/configuration.h \
    ../../src/app/core/log.h \
    ../../src/app/core/utils.h \
    ../../src/app/core/smartptr.h \
    ../../src/app/core/smartptr.inl \
    ../../src/app/data/events.h \
    ../../src/app/data/modelevent.h \
    ../../src/app/data/modeluser.h \
    ../../src/app/data/modellocation.h \
    ../../tests/testevents.h

#FORMS += \
#    ../../src/app/gui/forms/mainwindow.ui

#RESOURCES += \
#    ../../src/app/gui/resources/application.qrc
