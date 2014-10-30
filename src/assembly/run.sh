#!/bin/sh

if [ -d $JAVA_HOME ]; then
   echo "Found Java Home $JAVA_HOME"
else
   echo "Please set JAVA_HOME"
   exit
fi

export JAVA_HOME

EXE="$JAVA_HOME/bin/java -classpath ./conf:./lib/* com.sam.dbmAuto.impl.Main $@"

echo "Starting Run ..."

eval $EXE