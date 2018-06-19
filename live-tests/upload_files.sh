#!/bin/bash
set -e
EXECUTABLE="~/Dev/Android-SDK/platform-tools/adb"
SD_PATH="/sdcard/0/"
FILES=$(pwd)
echo "working in $FILES"
for f in *.csv
do
eval "$EXECUTABLE push "$f" $SD_PATH$f"
done
