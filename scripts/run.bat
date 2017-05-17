#!/bin/bash

LOGS_DIR=logs/
CONF_DIR=conf/

mkdir $LOGS_DIR

sudo ln -s /dev/ttyACM0 /dev/ttyS85
java -Djava.library.path=../lib -cp /usr/local/jdk1.8.0_121/jre/lib/ext/jfxrt.jar:'*'  com.dronegcs.console.controllers.DroneLaunch $LOGS_DIR $CONF_DIR> $LOGS_DIR/backlog.elg
