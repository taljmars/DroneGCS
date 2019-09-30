@echo off 
echo Deploy Console
7za.exe x Console* -oDroneConsole
cd DroneConsole
call install.bat
cd ..
echo "Done"

