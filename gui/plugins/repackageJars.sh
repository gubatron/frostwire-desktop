#!/bin/bash
#Recreate jars and make sure there are no .classes dangling around so we know we're using
#the jars and nothing else when we're running the tests.

rm -fr *.jar

#brooklyn
javac brooklyn/*.java
jar -cvf brooklyn.jar brooklyn/*
rm brooklyn/*.class

#twoScriptsTest
javac twoScriptsTest/**/*.java
jar -cvf twoScriptsTest.jar twoScriptsTest/*
rm twoScriptsTest/**/*.class
