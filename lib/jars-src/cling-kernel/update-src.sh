#!/bin/bash

CLING_SRC=/Users/aldenml/Development/sources/cling/core/src/main/java
SEAMLESS_UTIL_SRC=/Users/aldenml/Development/sources/seamless/util/src/main/java
SEAMLESS_XML_SRC=/Users/aldenml/Development/sources/seamless/xml/src/main/java


cd src

for f in `find . | egrep "(.*)java$"`
do
    cp $CLING_SRC/$f `dirname $f`
    cp $SEAMLESS_UTIL_SRC/$f `dirname $f`
    cp $SEAMLESS_XML_SRC/$f `dirname $f`
done

cd -