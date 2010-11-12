#!/bin/sh

ls -A1 | while read X
do
    if [ "$X" = "CVS" ]; then
        echo "ignoring cvs dir."
    elif [ -d "$X" ]; then
        fwtp=${X}_theme.fwtp
        if [ "$X" = "brushed_metal" ] || [ "$X" = "pinstripes" ] ; then
            export fwtp=${X}_theme_osx.fwtp
        fi
        
        rm -f $fwtp
        zip -0 -j $fwtp $X/*
    fi
done

rm -f themes.jar
jar -0Mcf themes.jar *.fwtp
rm -f *.fwtp
mv themes.jar ../jars/other/themes.jar

