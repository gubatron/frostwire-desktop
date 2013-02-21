#!/bin/bash

cd src

for f in `find . | egrep "(.*)java$"`
do
    echo $f
done

cd -