#!/bin/bash
set -e

if [ -z "$1" ]
  then
    echo "No file specified to download"
fi

EXECUTABLE="~/Dev/Android-SDK/platform-tools/adb"
SD_PATH="/sdcard/0/"
eval "$EXECUTABLE pull "$SD_PATH$1" $1"
