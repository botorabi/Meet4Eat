#!/bin/sh

echo "Meet4Eat, Copyright 2017-2018"
echo "https://m4e.org"
echo " "

if [ $# -ne 1 ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

DIR_INSTALL=Installers

cd MacOs
./build-dist-package-macox.sh $1 ../$DIR_INSTALL
cd ..
