@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_CREATE_SELF_SIGNED_CERTIFICATE
if %2.==. GOTO NO_NAME_CREATE_SELF_SIGNED_CERTIFICATE
if %3.==. GOTO NO_SUBJ_CREATE_SELF_SIGNED_CERTIFICATE

SET LOCATION=%~1
SET NAME=%~2
SET SUBJ=%~3
echo Subject: %~3

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%NAME%.pem
SET REQUEST_LOCATION=%LOCATION%\request\req_%NAME%.csr
SET CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.der
SET TEMP_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.pem

REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%NAME%.txt

REM remove files to create when they already exist
if exist "%PRIVATE_KEY_LOCATION%" del "%PRIVATE_KEY_LOCATION%"
if exist "%REQUEST_LOCATION%" del "%REQUEST_LOCATION%"
if exist "%CERTIFICATE_LOCATION%" del "%CERTIFICATE_LOCATION%"
if exist "%TEMP_CERTIFICATE_LOCATION%" del "%TEMP_CERTIFICATE_LOCATION%"
if exist "%CA_DATABASE_LOCATION%" del "%CA_DATABASE_LOCATION%"*
if exist "%CA_SERIAL_NUMBER_LOCATION%" del "%CA_SERIAL_NUMBER_LOCATION%"*
if exist "%CA_CRL_NUMBER_LOCATION%" del "%CA_CRL_NUMBER_LOCATION%"*

echo Root dir: %LOCATION%
echo Name: %NAME%
echo Subject: %SUBJ%

echo Step 1: Initialize location

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

echo Step 2: Creating CA related files
REM Generate an empty file
echo. 2> "%CA_DATABASE_LOCATION%"
echo 00 > "%CA_SERIAL_NUMBER_LOCATION%"
echo 00 > "%CA_CRL_NUMBER_LOCATION%"

echo Step 3: Creating private key
"%BATCH_DIR%openssl%X64%" genrsa -des3 -out "%PRIVATE_KEY_LOCATION%"  -passout pass:pass 2048

echo Step 4: Creating certificate request
"%BATCH_DIR%openssl%X64%" req -config "%CONFIGURATION%" -new -key "%PRIVATE_KEY_LOCATION%" -outform PEM -out "%REQUEST_LOCATION%"  -passin pass:pass

echo Step 5: Creating self signed cert
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -batch -days 365 -selfsign -keyfile "%PRIVATE_KEY_LOCATION%"  -in "%REQUEST_LOCATION%" -out "%TEMP_CERTIFICATE_LOCATION%"  -subj "%SUBJ%" -passin pass:pass -extensions v3_self_signed

REM Convert PEM certificate to DER format
"%BATCH_DIR%openssl%X64%" x509 -inform PEM -in "%TEMP_CERTIFICATE_LOCATION%" -outform DER -out "%CERTIFICATE_LOCATION%"

GOTO END_CREATE_SELF_SIGNED_CERTIFICATE

:NO_PATH_CREATE_SELF_SIGNED_CERTIFICATE
echo No path has been specified
GOTO USAGE

:NO_NAME_CREATE_SELF_SIGNED_CERTIFICATE
echo No name has been specified
GOTO USAGE

:NO_SUBJ_CREATE_SELF_SIGNED_CERTIFICATE
echo No subject has been specified
GOTO USAGE

:USAGE
echo Usage create_self_signed_certificate.bat 'location_root' 'unique_name' 'subj'
echo where subj has the form: "/C=%COUNTRY%/L=%LOCATION%/ST=%STATE%/O=%ORGANIZATION%/OU=%ORGANIZATION_UNIT%/CN=%TO%"
goto END_CREATE_SELF_SIGNED_CERTIFICATE

:END_CREATE_SELF_SIGNED_CERTIFICATE
