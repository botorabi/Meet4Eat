#!/bin/bash
# Meet4Eat Deployment Package Builder for Linux
# Copyright 2017-2018
# https://m4e.org


if [ $# -ne 1 ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

DIR_INSTALL=Installers
mkdir -p $DIR_INSTALL

cd Linux
./build-dist-package-linux.sh $1 ../$DIR_INSTALL
cd ..

