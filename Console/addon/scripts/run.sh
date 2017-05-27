#!/bin/bash

LOGS_DIR=logs/
CONF_DIR=conf/

sudo ln -s /dev/ttyACM0 /dev/ttyS85
java -Dloader.path=lib/ -Djava.library.path=native/ -cp GCDashboard.jar:/usr/local/jdk1.8.0_121/jre/lib/ext/jfxrt.jar:./lib org.springframework.boot.loader.PropertiesLauncher %LOGS_DIR% %CONF_DIR% > %LOGS_DIR%/backlog.elg
