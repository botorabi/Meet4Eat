#!/bin/sh

echo "Meet4Eat, Copyright 2017-2018"
echo "https://m4e.org"
echo " "

if [ $# -ne 2 ]; then
  echo "Usage: $0 <version> <installation path>"
  exit 1
fi

echo "Creating distribution package..."

VERSION=$1
DIR_INSTALL=$2
APP_NAME=Meet4Eat

DIR_DIST=.
DIR_BUILD=build-app-Desktop-Qt-macos
DIR_QT=/Developer/Qt/5.9.3/clang_64
PKG_MAKER="/Applications/PackageMaker.app/Contents/MacOS/PackageMaker"

export PATH=$PATH:$DIR_QT/bin/

mkdir -p $DIR_INSTALL
rm -rf $APP_NAME.app
rm -rf $DIR_BUILD
mkdir $DIR_BUILD
cd $DIR_BUILD

echo "Building binaries"
qmake ../../../build/app/app.pro -r -spec macx-clang
make -j4
echo "Creating application package"
macdeployqt $APP_NAME.app
mv $APP_NAME.app ../
cd ..
echo "Application successfully created: " $DIR_DIST/$APP_NAME.app

rm -f $DIR_DIST/$APP_NAME-macos-v$VERSION.pkg
${PKG_MAKER} --doc $APP_NAME-PkgMaker.pmdoc --out $DIR_DIST/$APP_NAME-macos-v$VERSION.pkg
mv $DIR_DIST/$APP_NAME-macos-v$VERSION.pkg $DIR_INSTALL/
rm -rf $DIR_BUILD
rm -rf $APP_NAME.app
echo "Package successfully created: " $DIR_INSTALL/$APP_NAME-macos-v$VERSION.pkg
