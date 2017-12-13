#!/bin/bash
# Meet4Eat Deployment Package Builder
# Copyright 2017-2018
# https://m4e.org
#
# Version 1.1

#set -x

echo "Meet4Eat, Copyright 2017-2018"
echo "https://m4e.org"
echo " "

echo "Creating distribution package..."

if [ -z "$QT_DIR" ]; then
  echo "*** Please set QT_DIR before using this script!"
  exit 1
fi

if [ $# -ne 2 ]; then
  echo "Usage: $0 <version> <output dir> "
  exit 1
fi

# Application name also used for package name
APP_NAME=Meet4Eat

# this is the folder containing the distribution files
DIST_FILES_DIR=DIST_FILES

PLATFORM=linux

TMP_DIR=_tmp
VERSION=$1
OUTPUT_DIR=$2
DIST_DIR=$TMP_DIR/$APP_NAME-v$VERSION
TEMPLATE_DIR=templates

PACKAGE_DIR=$TMP_DIR/package
PACKAGE_TAR=$TMP_DIR/package.txz
BUILD_DIR=build-app-Desktop-Qt-mswin

APPBINARY=$TMP_DIR/$BUILD_DIR/$APP_NAME
RUNSCRIPT=$TEMPLATE_DIR/run-template.sh
DEPLOYFILE=$TMP_DIR/$APP_NAME.sh

echo "Using QT_DIR=$QT_DIR"

rm -rf $TMP_DIR
mkdir -p $TMP_DIR/$BUILD_DIR

echo "Building the application"
cd $TMP_DIR/$BUILD_DIR
pwd
$QT_DIR/bin/qmake ../../../../build/app/app.pro -r -spec linux-g++
make -j4 -f Makefile all
cd -

echo "Create application package"
mkdir $PACKAGE_DIR
mkdir $PACKAGE_DIR/lib

QTLIBS=$(ldd $APPBINARY | grep libQt | cut -d ' ' -f 3)
ADDTIONAL_QTLIBS=(libQt5XcbQpa.so.5 libQt5DBus.so.5 libicui18n.so.56 libicudata.so.56 libicuuc.so.56 )
PLUGINS=(platforms imageformats xcbglintegrations)
for f in $QTLIBS; do 
  cp -v $f $PACKAGE_DIR/lib/
done
for f in ${ADDTIONAL_QTLIBS[@]}; do
  cp -v $QT_DIR/lib/$f $PACKAGE_DIR/lib/
done
cp -r ${QT_DIR}/libexec $PACKAGE_DIR/

mkdir $PACKAGE_DIR/plugins
for p in ${PLUGINS[@]}; do
cp -r ${QT_DIR}/plugins/$p $PACKAGE_DIR/plugins/
done

cp -r $QT_DIR/resources $PACKAGE_DIR/
mkdir $PACKAGE_DIR/translations
cp $TEMPLATE_DIR/qt.conf $PACKAGE_DIR/
cp $TEMPLATE_DIR/libexec.qt.conf $PACKAGE_DIR/libexec/qt.conf
cp $TEMPLATE_DIR/Meet4Eat.desktop $PACKAGE_DIR/
cp $TEMPLATE_DIR/Meet4Eat.autostart $PACKAGE_DIR/
cp $DIST_FILES_DIR/Meet4Eat.png $PACKAGE_DIR/
cp $APPBINARY $PACKAGE_DIR/
cat $RUNSCRIPT | sed "s#APPVERSION#${VERSION}#" > $DEPLOYFILE

tar cJvf $PACKAGE_TAR -C $PACKAGE_DIR .

echo "PAYLOAD:" >> $DEPLOYFILE
cat $PACKAGE_TAR >> $DEPLOYFILE
chmod 755 $DEPLOYFILE

echo "Creating distribution package"
mkdir $DIST_DIR
mv $DEPLOYFILE $DIST_DIR/
cp $DIST_FILES_DIR/LICENSE $DIST_DIR/
cat $DIST_FILES_DIR/README.md | sed "s#APPVERSION#${VERSION}#" > $DIST_DIR/README.md

cd $TMP_DIR
tar cf $APP_NAME-v$VERSION.tar $APP_NAME-v$VERSION/
bzip2 $APP_NAME-v$VERSION.tar
cd ..

OUTPUT_FILENAME=$APP_NAME-$PLATFORM-v$VERSION.tar.bz2
mv $DIST_DIR.tar.bz2 $OUTPUT_DIR/$OUTPUT_FILENAME
echo " "
echo "Distribution package successfully created: " $OUTPUT_DIR/$OUTPUT_FILENAME
rm -rf $TMP_DIR

