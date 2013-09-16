#!/bin/bash

portableSource=$1
portableTarget=$2

function IsFrostWireRunning() {
    RESULT=`pgrep Finder`
    if [ "${RESULT:-null}" = null ]; then
        echo "0"
    else
        echo "1"
    fi
}

function WaitFrostWireStopped() {
    for (( i=1; i<=30; i++ ))
    do
        if [ "$(IsFrostWireRunning)" = "0" ]; then
            echo "1"
            return
        fi

        sleep 1s
    done
    
    echo "0"
}

function CopyFrostWireFiles() {
    `rm -rf $portableTarget`
    `mv $portableSource $portableTarget`
}

function LaunchFrostWire() {
    # only for Mac OS X for now
    `open $portableTarget`
}

if [ "$(WaitFrostWireStopped)" = "1" ]; then
    CopyFrostWireFiles
    LaunchFrostWire
fi