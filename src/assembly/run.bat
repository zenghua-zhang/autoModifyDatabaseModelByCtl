@echo off

SET RMODE=%1
SET PARAMS=
:LOOP
if "x%1" == "x" GOTO ENDLOOP
SET PARAMS=%PARAMS% %1
SHIFT
GOTO LOOP
:ENDLOOP

if  "x%RMODE%" == "x" GOTO ECHO_DEV
GOTO ECHO_MODE

:ECHO_DEV
echo "Warning; running in standard dev mode"
GOTO JAVA_CHECK
:ECHO_MODE
echo "Running using %RMODE%"
:JAVA_CHECK
if "x%JAVA_HOME%" == "x"  GOTO ECHO_EXIT
echo "Found Java Home %JAVA_HOME%"
GOTO SET_CLASSPATH_AND_RUN
ECHO_EXIT
   echo "Please set JAVA_HOME"
   exit
:SET_CLASSPATH_AND_RUN

echo "Starting run ..."

%JAVA_HOME%\bin\java -classpath ./conf;./lib/*;%CLASSPATH% com.sam.dbmAuto.impl.Main %PARAMS%
