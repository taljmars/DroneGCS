@echo off 

echo Deploy Standalone
echo -----------------

echo (1/4) Download Framework
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/7za.exe -o 7za.exe

echo (2/4) Download Drone Server
curl https://raw.githubusercontent.com/taljmars/DroneServer/master/ServerInstallation/Windows64-lite/ServerCore-2.1.4.RELEASE-win-x64-lite.zip -o Server-win-x64-lite.zip


echo (3/4) Download Drone Console
curl https://raw.githubusercontent.com/taljmars/DroneGCS/master/ClientInstallation/Windows/DroneGCS-win-x64.zip -o Console-win-x64.zip


echo (4/4) Installing Components
7za.exe x Server* -oDroneServer
7za.exe x Console* -oDroneConsole

echo cd DroneServer > runDroneGCS.bat
echo start /b run.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat
echo cd DroneConsole >> runDroneGCS.bat
echo start /b run.bat >> runDroneGCS.bat
echo cd .. >> runDroneGCS.bat

echo Done  
