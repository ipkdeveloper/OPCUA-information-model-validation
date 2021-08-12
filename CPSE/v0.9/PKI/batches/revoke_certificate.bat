@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_REVOKE
if %2.==. GOTO NO_CA_NAME
if %3.==. GOTO NO_NAME_REVOKE

SET LOCATION=%~1
SET CA_NAME=%~2
SET NAME=%~3

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET CA_CERTIFICATE=%LOCATION%\certs\cert_%CA_NAME%.pem
SET CA_PRIVATE_KEY=%LOCATION%\private\private_key_%CA_NAME%.pem
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%CA_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%CA_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%CA_NAME%.txt

SET DER_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.der
SET PEM_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.pem

echo Root dir: %LOCATION%
echo Name: %NAME%
echo DER Certificate location: %DER_CERTIFICATE_LOCATION%
echo PEM Certificate location: %PEM_CERTIFICATE_LOCATION%
echo Configuration file: %CONFIGURATION%

echo Step 1: Initialize location

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

echo Step 2: Convert the DER certificate to PEM
"%BATCH_DIR%openssl%X64%" x509 -inform DER -in "%DER_CERTIFICATE_LOCATION%" -outform PEM -out "%PEM_CERTIFICATE_LOCATION%"

echo Step 3: Revoke certificate
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -revoke "%PEM_CERTIFICATE_LOCATION%" -passin pass:pass -cert "%CA_CERTIFICATE%" -keyfile "%CA_PRIVATE_KEY%"

GOTO END_REVOKE

:NO_PATH_REVOKE
echo No path has been specified
GOTO USAGE

:NO_CA_NAME
echo No CA name has been specified
GOTO USAGE

:NO_NAME_REVOKE
echo No name has been specified
GOTO USAGE

:USAGE
echo Usage revoke_certificate.bat 'location_root' 'ca_name' 'unique_name'
echo where ca_name is the filename without file extention (e.g. myCA)
GOTO END_REVOKE

:END_REVOKE
