@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_UPDATE_CRL
if %2.==. GOTO NO_CA_NAME

SET LOCATION=%~1
SET CA_NAME=%~2

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET REVOCATION_LIST_LOCATION=%LOCATION%\crl\revocation_list_%CA_NAME%.crl
SET CA_CERTIFICATE=%LOCATION%\certs\cert_%CA_NAME%.pem
SET CA_PRIVATE_KEY=%LOCATION%\private\private_key_%CA_NAME%.pem
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%CA_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%CA_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%CA_NAME%.txt

echo Root dir: %LOCATION%
echo Configuration file: %CONFIGURATION%

echo Step 1: Initialize location

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

echo Step 2: Generate CRL
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -gencrl -crldays 30 -out "%REVOCATION_LIST_LOCATION%" -passin pass:pass -cert "%CA_CERTIFICATE%" -keyfile "%CA_PRIVATE_KEY%"

GOTO END_UPDATE_CRL

:NO_PATH_UPDATE_CRL
echo No path has been specified
GOTO USAGE

:NO_CA_NAME
echo No CA name has been specified
GOTO USAGE

:USAGE
echo Usage update_revokation_list.bat 'location_root' 'ca_name'
echo where ca_name is the filename without file extention (e.g. myCA)
GOTO END_UPDATE_CRL

:END_UPDATE_CRL
