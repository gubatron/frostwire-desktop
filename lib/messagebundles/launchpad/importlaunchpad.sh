#!/bin/bash
# @Author Fernando Toussaint (FTA)
# This script import (replaces) the current translation files for the ones in Launchpad

convertlaunchpadfiles() {   
    for i in frostwire/frostwire-*.po; do
      echo "Converting $i...";        
      mv "$i" "${i#frostwire/frostwire-}"; 
    done    
}

updatelaunchpadfiles() {
    echo "Updating .po files...";
    
    for i in *.po; do          
      mv "$i" "../$i"; 
    done
    echo "New translation files updated!"
}

ugradelaunchpadfiles() {
        echo "Would you like to generate a new .jar file with the new translations? (Y/n): "
        read generatejar

        if [ $generatejar = 'y' -o $generatejar = 'Y' ]; then
                CURRENTDIR=`pwd`
                echo "Generating new .jar file..."                
                cd ..
                "./convert.sh"
                cd $CURRENTDIR
                echo "New files have been updated and upgraded to the new translations!"
                echo "You may run FrostWire now and see the changes".
                exit;
        else
                echo "The new translations have been updated correctly but you will need to generate manually the new .jar file to see the changes in FrostWire."
        fi
}

untarfirst() {
    if [ -f launchpad-export.tar.gz ]
    then
      echo "Decompressing po files...";
      tar -zxvf launchpad-export.tar.gz    
    else
      echo "Please copy the latest file 'launchpad-export.tar.gz' exported by launchpad to this directory first."
      echo "This file contains the new translations for FrostWire."
      exit;
    fi
    
}

untarfirst
convertlaunchpadfiles
updatelaunchpadfiles
ugradelaunchpadfiles