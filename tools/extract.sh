#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA_PROJ_LIB=../lib

for f in $JAVA_PROJ_LIB/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

JAVA=$JAVA_HOME/bin/java
JAVA_HEAP_MAX=-Xmx5000m
JAVA_PARAMS=-d64

CLASS=WikiExtractPairs

exec nohup nice "$JAVA" $JAVA_HEAP_MAX $JAVA_PARAMS -classpath "$CLASSPATH" $CLASS "$@" &
