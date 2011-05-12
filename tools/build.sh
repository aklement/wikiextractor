#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA_PROJ_LIB=../lib
JAVA_PROJ_SRC=../src
JAVA_PROJ_BIN=../bin

for f in $JAVA_PROJ_LIB/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

JAVAC=$JAVA_HOME/bin/javac
JAR=$JAVA_HOME/bin/jar

mkdir -p $JAVA_PROJ_BIN
echo "Builging..."
$JAVAC -classpath $CLASSPATH -d $JAVA_PROJ_BIN -sourcepath $JAVA_PROJ_SRC $JAVA_PROJ_SRC/WikiExtractPairs.java
echo "Making a jar..."
$JAR cvf $JAVA_PROJ_LIB/wikiextractor.jar -C $JAVA_PROJ_BIN .
