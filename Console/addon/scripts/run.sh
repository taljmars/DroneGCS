#!/bin/bash

LOGS_DIR=logs/
CONF_DIR=conf/

if [ -e /dev/ttyACM0 ];then
	if [ ! -e /dev/ttyS85 ]; then
		sudo ln -s /dev/ttyACM0 /dev/ttyS85
	fi
fi
java -Dloader.path=lib/ -Djava.library.path=native/ -cp GCDashboard.jar:./lib org.springframework.boot.loader.PropertiesLauncher $LOGS_DIR $CONF_DIR &> $LOGS_DIR/backlog.elg
