@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_CERTIFICATE_REQUEST

SET LOCATION=%~1

SET DHPARAM_FILE_LOCATION=%LOCATION%\private\dhparam.pem

REM remove files to create when they already exist
if exist "%DHPARAM_FILE_LOCATION%" del "%DHPARAM_FILE_LOCATION%"

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"

REM Generate dhparam file (this can take a while)
"%BATCH_DIR%openssl%X64%" dhparam -outform PEM -out "%DHPARAM_FILE_LOCATION%" 2048

GOTO END_DHPARAM_FILE

:NO_PATH_CERTIFICATE_REQUEST
echo No path has been specified
GOTO USAGE

:USAGE
echo Usage create_dhparam_file.bat 'location_root'
GOTO END_DHPARAM_FILE

:END_DHPARAM_FILE

