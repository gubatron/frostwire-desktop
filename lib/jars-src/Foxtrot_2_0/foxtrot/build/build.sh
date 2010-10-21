#!/bin/sh

if test -z "${JAVA_HOME}" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    exit
fi

export CLASSPATH=ant.jar
export CLASSPATH=$CLASSPATH:optional.jar
export CLASSPATH=$CLASSPATH:jaxp.jar
export CLASSPATH=$CLASSPATH:crimson.jar
export CLASSPATH=$CLASSPATH:../lib/junit.jar
export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar

$JAVA_HOME/bin/java -classpath $CLASSPATH org.apache.tools.ant.Main $@
