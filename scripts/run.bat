#!/bin/bash

LOGS_DIR=logs/
CONF_DIR=conf/

echo "Staring DroneGCS"
mkdir $LOGS_DIR

echo "Verify USB device"
if [ ! -e /dev/ttyS85 ]; then
	echo "Creating device link" 
	sudo ln -s /dev/ttyACM0 /dev/ttyS85
fi
echo "Device link created" 

echo "Starting dashboard" 
java -Djava.library.path=../lib -cp /usr/local/jdk1.8.0_121/jre/lib/ext/jfxrt.jar:'*'  com.dronegcs.console.controllers.DroneLaunch $LOGS_DIR $CONF_DIR> $LOGS_DIR/backlog.elg
