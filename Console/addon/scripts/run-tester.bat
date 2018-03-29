@ECHO OFF
@SET LOGS_DIR=logs/
@SET CONF_DIR=conf/

java -cp jars/* com.drone_tester.Tester %LOGS_DIR% %CONF_DIR% org.springframework.boot.loader.PropertiesLauncher -Dloader.path=lib/ -Djava.library.path=./native > %LOGS_DIR%/backlog.elg
