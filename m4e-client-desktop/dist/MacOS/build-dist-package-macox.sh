#!/bin/sh

echo "Meet4Eat, Copyright 2017"
echo "https://m4e.org"
echo " "

echo "Creating distribution package..."

if [ -z "$QT_DIR" ]; then
  echo "*** Please set QT_DIR before using this script!"
  exit 1
fi

if [ $# -ne 2 ]; then
	echo "Usage: $0 <version> <output dir>"
	exit 1
fi


VERSION=$1
DIR_DIST=$2

DIR_BUILD=build-app-Desktop-Qt-macos
DIR_QT=/Developer/Qt/5.9.3/clang_64

export PATH=$PATH:$DIR_QT/bin/

rm -rf $DIR_BUILD
mkdir $DIR_BUILD
cd $DIR_BUILD

echo "Building binaries"
qmake ../../../build/app/app.pro -r -spec macx-clang
make -j4
echo "Creating deployment package"
macdeployqt Meet4Eat.app
cd ..

echo "Pacakge successfully created: " $DIR_DIST/Meet4Eat-macos-v$VERSION.app
rm -rf $DIR_DIST/Meet4Eat-macos-v$VERSION.app
cp -r $DIR_BUILD/Meet4Eat.app $DIR_DIST/Meet4Eat-macos-v$VERSION.app


