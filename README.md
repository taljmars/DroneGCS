# DroneGCS

The following directory hold the DroneGCS project files for the GUI and Controller components. It consist of code, drivers and executable jars directory.
In order to have a better understanding of the code simply dig into it.
The controller components exist in the GUI Plugin is responsible of two things, the first, communicate with the drone
itself, running mission, flight control and many other nice utility. It second hat is to communicate with the server
out there in the cloud, the server responsible of saving you configuration, saving your mission and perimeters.
The server gives you a private DB to work on until you save it to the public DB.

If you are a user who wishes to fly you drone and enjoy this fully featured Ground Control Station, simple download the relevant files from ClientInstallation directory: https://github.com/taljmars/DroneGCS/tree/master/ClientInstallation, Run Deploy.bat and enjoy your newly GCS, this client require an internet connection. In case you wish to use local server (For having better performance and offline flight, download DroneServer as well: https://github.com/taljmars/DroneServer/tree/master/ServerInstallation and follow it installation guidelines)
Last but not least, in case you wish to install a standalone version which include both client and server in a single installer please refere to this repository: https://github.com/taljmars/StandaloneDroneGCS

### Important Folder
#### ExTool
Holds the relevant drivers needed to be installed on the relevant PC to support antenna and other physical components.
#### ExLib
Holds the external libraries needed to be add to Eclipse/IntelIJ (Or other) IDE for developing.
You may also use startModule.sh script to fetch relevant libraries. the script will recognize your OS type and
update the relevant files automatically.
In case your purpose is to develop over this code, remember one important thing.
"Clean" action of maven will also remove this libraries, therefor you will have to run the script again.  
This is a temporary problem that will be solved later.
#### ExRunnable
Holds all the files needed to run the GCS application, it is a compilation results.
You may download it to your system and run it with "run" script exist in the directory.

### Resources
The project is based on Spring framework (4.3.3), a good info about the release can be found in this link
http://repo.spring.io/release/org/springframework/spring/4.3.3.RELEASE/

DigitalCloud supply a Tomcat server and a nice environment to hold the drone server.

The map resource and main code section are being supported using JMapViewer project of OSM (OpenStreetMap)
http://wiki.openstreetmap.org/wiki/JMapViewer

Contact:
taljmars@gmail.com

Tal.
