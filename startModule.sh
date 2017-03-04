#!/bin/bash

EXT_MODULE_DIR=tmp
mkdir $EXT_MODULE_DIR

# Machines type 64/32
MACHINE_TYPE=`uname -m`
if [ ${MACHINE_TYPE} == 'x86_64' ]; then
	MACHINE_FLAVOUR=64
else
	MACHINE_FLAVOUR=32
fi


function fetch {
	MODULE=$1
	echo "Fetching module '${MODULE}'"
	echo "Getting 'https://github.com/taljmars/${MODULE}/archive'"
	wget -O $EXT_MODULE_DIR/$MODULE.zip https://github.com/taljmars/$MODULE/archive/master.zip
	chmod 777 $EXT_MODULE_DIR/$MODULE.zip
	unzip $EXT_MODULE_DIR/$MODULE.zip -d $EXT_MODULE_DIR
	rm $EXT_MODULE_DIR/$MODULE.zip
	mv $EXT_MODULE_DIR/$MODULE* $EXT_MODULE_DIR/$MODULE
	echo Done
}

function handlingOp {
	BASE=$1
	OS=$2
	FLAVOUR=$3
	TYPE=$4
	FILES=$5
	TITLE=$6

	if [ "X$OS" != "XLinux" ] && [ "X$OS" != "XALL" ]; then
		return
	fi

	if [ "X$FLAVOUR" != "X$MACHINE_FLAVOUR" ] && [ "X$FLAVOUR" != "XALL" ]; then
		return
	fi

	if [ "X$TYPE" == "XDLL" ]; then
		echo Handling DLLs
		echo $FILES
		cp -p ${BASE}/$FILES/* CMpub/lib/
	else
		echo Handling Jars
		echo ${BASE}/${FILES}
		mvn install:install-file -Dfile=${BASE}/${FILES} -DgroupId=$TITLE -DartifactId=$TITLE -Dversion=1.0 -Dpackaging=jar -DgeneratePom=false
	fi
}

#
# maven.jar.config file should be placed ina directory where we want to install a non maven jar(s)
# the file should contain a jar names in each line (without the .jar suffix)
#
function updateMavenRepository {
	REPO=$1
	echo "Updating Maven Repository of '$REPO'"
	file="$EXT_MODULE_DIR/$REPO/maven.jar.config"
	while read -r line; do
		[[ "$line" =~ ^#.*$ ]] && continue
		[[ "X$line" == "X" ]] && continue
		stringarray=($line)
		OS=${stringarray[0]}
		FLAVOUR=${stringarray[1]}
		TYPE=${stringarray[2]}
		FILES=${stringarray[3]}
		TITLE=${stringarray[4]}
		handlingOp $EXT_MODULE_DIR/$REPO $OS $FLAVOUR $TYPE $FILES $TITLE
	done < "$file"
}

function clearDirectories {
	sudo rm -rf $1
}

echo "Creating external modules directory '${EXT_MODULE_DIR}'"
mkdir -p $EXT_MODULE_DIR
mkdir -p CMpub/lib

file="./ModulesDep"
while read -r line; do
	[[ "$line" =~ ^#.*$ ]] && continue
	[[ "X$line" == "X" ]] && continue
	echo "Loading module '${line}'"
	fetch $line
	updateMavenRepository ${line}
	clearDirectories $EXT_MODULE_DIR/${line}
done < "$file"

clearDirectories $EXT_MODULE_DIR

echo "Done"








