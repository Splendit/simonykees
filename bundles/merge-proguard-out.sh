#!/bin/bash

TARGET="$( cd "$(dirname "$0")" ; pwd -P )"/target

# creates target dir if it does not exist
mkdir -p $TARGET
touch $TARGET/mapping.in

#merges all produced out files together if some exist
cnt=$(ls $TARGET/*.out 2>/dev/null | wc -l)
if [ $cnt -gt 0 ]; then
  cat $TARGET/*.out > $TARGET/mapping.in
fi
