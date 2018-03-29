@ECHO OFF
set LOGS_DIR=logs/
set CONF_DIR=conf/

java -cp jars/* com.drone_tester.Tester %LOGS_DIR% %CONF_DIR% org.springframework.boot.loader.PropertiesLauncher -Dloader.path=lib/ -Djava.library.path=./native > %LOGS_DIR%/backlog.elg
