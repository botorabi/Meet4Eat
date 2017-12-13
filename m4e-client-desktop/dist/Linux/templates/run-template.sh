#!/bin/bash

# Meet4Eat Application Starter
# Copyright 2017-2018
# https://m4e.org

#set -x
VERSION=APPVERSION
INSTALLDIR=./bin
ASCFILE=$INSTALLDIR/Meet4Eat.asc
RUN_SCRIPT="$PWD/$0"
FORCE=0
PROGRAM=${0##.*/}

function untar_payload()
{
  match=$(grep --text --line-number '^PAYLOAD:$' "$RUN_SCRIPT" | cut -d ':' -f 1)
  payload_start=$((match + 1))
  tail -n +$payload_start "$RUN_SCRIPT" | tar -xJvf - 
}

function update_autostart()
{
  cd $INSTALLDIR
  LAST_DIR=""
  if [ -f Meet4Eat.dir ]; then
    LAST_DIR=`cat Meet4Eat.dir`
  fi

  CURR_DIR=`pwd`
  cd -
  if [ "$LAST_DIR" != "$CURR_DIR" ]; then
    echo "updating the auto-start and desktop entries"
    echo "$CURR_DIR" > $INSTALLDIR/Meet4Eat.dir
    DSK_AUTOSTART=$(cat "$CURR_DIR/Meet4Eat.autostart")
    echo "${DSK_AUTOSTART//\$(INST_DIR)/"$CURR_DIR/"}" > ~/.config/autostart/Meet4Eat.desktop
    DSK_DESKTOP=$(cat "$CURR_DIR/Meet4Eat.desktop")
    echo "${DSK_DESKTOP//\$(INST_DIR)/"$CURR_DIR/"}" > ~/.local/share/applications/Meet4Eat.desktop
  fi
}

# Check, if this skript has changed! If it has changed, remove the installdir to force reinstall
[ -f "$ASCFILE" ] || FORCE=1
[ $FORCE -eq 0  ] && sha256sum -c "$ASCFILE" --status || rm -rf $INSTALLDIR

if [ ! -d $INSTALLDIR -o  $FORCE -eq 1 ]; then
  mkdir $INSTALLDIR
  cd $INSTALLDIR
  untar_payload
  sha256sum "$RUN_SCRIPT" > Meet4Eat.asc
  cd - 
fi

update_autostart

export LD_LIBRARY_PATH=$INSTALLDIR/lib:$LD_LIBRARY_PATH
$INSTALLDIR/Meet4Eat $*
exit 0
