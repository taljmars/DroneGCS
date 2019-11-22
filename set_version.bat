:: This batch file checks for network connection problems.
@ECHO OFF
echo Getting version

set /p mytextfile=< %~dp0\Console\src\main\resources\version
echo Version: %mytextfile%
set /a newversion = %mytextfile% + 1
echo New Version: %newversion%
echo %newversion% > %~dp0\Console\src\main\resources\version

exit 0

