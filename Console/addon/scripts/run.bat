@ECHO OFF
set LOGS_DIR=logs/
set CONF_DIR=conf/

java -Dloader.path=lib/ -Djava.library.path=./native -cp GCDashboard.jar:./lib org.springframework.boot.loader.PropertiesLauncher %LOGS_DIR% %CONF_DIR% > %LOGS_DIR%/backlog.elg
