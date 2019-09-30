@echo off 

echo Deploy Standalone

echo Download Framework
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/7za.exe -o 7za.exe

echo Download Drone Server
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/ServerCore-2.1.4.RELEASE-win-x64-lite.zip -o ServerCore-2.1.4.RELEASE-win-x64-lite.zip
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/deployServer.bat -o deployServer.bat

echo Download Drone Console
curl https://raw.githubusercontent.com/taljmars/DroneGCS/master/ClientInstallation/Windows/Console-1.0.0-SNAPSHOT-win-x64.zip -o Console-1.0.0-SNAPSHOT-win-x64.zip
curl https://raw.githubusercontent.com/taljmars/DroneGCS/master/ClientInstallation/Windows/deployConsole.bat -o deployConsole.bat

echo Installing Components
call deployServer.bat
call deployConsole.bat

echo cd ServerCore > runDroneGCS.bat
echo start /b install.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat
echo cd DroneConsole >> runDroneGCS.bat
echo start /b install.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat

echo Done  
