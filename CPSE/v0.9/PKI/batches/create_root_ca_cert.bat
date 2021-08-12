@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH
if %2.==. GOTO NO_CA_NAME
if %3.==. GOTO NO_SUBJ

SET LOCATION=%~1
SET CA_NAME=%~2
SET SUBJ=%~3

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET CA_PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%CA_NAME%.pem
SET CA_REQUEST_LOCATION=%LOCATION%\request\req_%CA_NAME%.csr
SET CA_PEM_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%CA_NAME%.pem
SET CA_DER_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%CA_NAME%.der
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%CA_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%CA_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%CA_NAME%.txt

REM remove files to create when they already exist
if exist "%CA_PRIVATE_KEY_LOCATION%" del "%CA_PRIVATE_KEY_LOCATION%"
if exist "%CA_REQUEST_LOCATION%" del "%CA_REQUEST_LOCATION%"
if exist "%CA_PEM_CERTIFICATE_LOCATION%" del "%CA_PEM_CERTIFICATE_LOCATION%"
if exist "%CA_DER_CERTIFICATE_LOCATION%" del "%CA_DER_CERTIFICATE_LOCATION%"
if exist "%CA_DATABASE_LOCATION%" del "%CA_DATABASE_LOCATION%"*
if exist "%CA_SERIAL_NUMBER_LOCATION%" del "%CA_SERIAL_NUMBER_LOCATION%"*
if exist "%CA_CRL_NUMBER_LOCATION%" del "%CA_CRL_NUMBER_LOCATION%"*

echo Root dir: %LOCATION%
echo CA private key location: %CA_PRIVATE_KEY_LOCATION%
echo CA certificate location: %CA_PEM_CERTIFICATE_LOCATION%
echo Creating CA root certificate

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

echo Step 3: Creating the files using OpenSSL
REM Generate private key
"%BATCH_DIR%openssl%X64%" genrsa -des3 -out "%CA_PRIVATE_KEY_LOCATION%" -passout pass:pass 2048 
REM Generate request key
"%BATCH_DIR%openssl%X64%" req -config "%CONFIGURATION%" -new -key "%CA_PRIVATE_KEY_LOCATION%" -outform PEM -out "%CA_REQUEST_LOCATION%" -passin pass:pass
REM Sign request, certificate in PEM format will be generated
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -batch -days 3650 -in "%CA_REQUEST_LOCATION%" -extensions v3_ca -out "%CA_PEM_CERTIFICATE_LOCATION%" -subj "%SUBJ%" -passin pass:pass -selfsign -keyfile "%CA_PRIVATE_KEY_LOCATION%"
REM Convert PEM certificate to DER format
"%BATCH_DIR%openssl%X64%" x509 -inform PEM -in "%CA_PEM_CERTIFICATE_LOCATION%" -outform DER -out "%CA_DER_CERTIFICATE_LOCATION%"

echo Step 4: Convert CA (root) certificate, in PEM format
"%BATCH_DIR%openssl%X64%" x509 -inform DER -in "%CA_DER_CERTIFICATE_LOCATION%" -outform PEM -out "%CA_PEM_CERTIFICATE_LOCATION%"

GOTO END

:NO_PATH
echo No path has been specified
GOTO USAGE

:NO_CA_NAME
echo No CA name has been specified
GOTO USAGE

:NO_SUBJ
echo No subject has been specified
GOTO USAGE

:USAGE
echo Usage create_root_ca_cert.bat 'location_root' 'ca_name' 'subj'
echo where ca_name is the filename without file extention (e.g. myCA)
echo and subj has the form: "/C=%COUNTRY%/L=%LOCATION%/ST=%STATE%/O=%ORGANIZATION%/OU=%ORGANIZATION_UNIT%/CN=%TO%"
GOTO END

:END
