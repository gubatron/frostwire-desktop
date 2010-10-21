@echo off
SETLOCAL ENABLEDELAYEDEXPANSION 
SET PATH=%PATH%;..\lib\native\windows
SET CLASSPATH=.;..\core

REM Get all common jars.
FOR %%j IN (..\lib\jars\*.jar) DO (
  SET CLASSPATH=!CLASSPATH!;%%j
)

REM Get all other jars.
FOR %%j IN (..\lib\jars\other\*.jar) DO (
  SET CLASSPATH=!CLASSPATH!;%%j
)

REM Get all windows jars.
FOR %%j IN (..\lib\jars\windows\*.jar) DO (
  SET CLASSPATH=!CLASSPATH!;%%j
)

REM Get all components
FOR /D %%c IN (..\components\*) DO (
  IF EXIST %%c\src (
    SET CLASSPATH=!CLASSPATH!;%%c\build\classes;%%c\src\main\resources
  )
)


java -Xms32m -Xmx128m -Ddebug=1 -Djava.net.preferIPV6Addresses=false -ea -Djava.net.preferIPv4stack=true com.limegroup.gnutella.gui.Main
