@echo off 

echo Deploy Standalone

echo Download Framework
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/7za.exe -o 7za.exe

echo Download Drone Server
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Linux-lite/ServerCore-2.1.5.RELEASE-linux.tar -o ServerCore-2.1.5.RELEASE-linux.tar
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/deployServer.bat -o deployServer.bat

echo Download Drone Console
copy ServerCore-2.1.5.RELEASE-linux.tar Console-2.1.5.RELEASE-linux.tar
curl https://raw.githubusercontent.com/taljmars/DroneGCS/master/ClientInstallation/Windows/deployConsole.bat -o deployConsole.bat

echo Installing Components
call deployServer.bat
call deployConsole.bat

echo cd ServerCore > runDroneGCS.bat
echo start /b run.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat
echo cd DroneConsole >> runDroneGCS.bat
echo start /b run.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat

echo Done  
