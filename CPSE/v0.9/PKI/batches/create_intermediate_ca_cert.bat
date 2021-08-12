@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH
if %2.==. GOTO NO_SIGNING_CA
if %3.==. GOTO NO_TARGET_CA
if %4.==. GOTO NO_SUBJ

SET LOCATION=%~1
SET SIGNING_CA_NAME=%~2
SET TARGET_CA_NAME=%~3
SET SUBJ=%~4

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET SIGNING_CA_PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%SIGNING_CA_NAME%.pem
SET SIGNING_CA_DER_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%SIGNING_CA_NAME%.der
SET SIGNING_CA_PEM_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%SIGNING_CA_NAME%.pem
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%SIGNING_CA_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%SIGNING_CA_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%SIGNING_CA_NAME%.txt

SET TARGET_CA_PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%TARGET_CA_NAME%.pem
SET TARGET_CA_REQUEST_LOCATION=%LOCATION%\request\req_%TARGET_CA_NAME%.csr
SET TARGET_CA_DER_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%TARGET_CA_NAME%.der
SET TARGET_CA_PEM_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%TARGET_CA_NAME%.pem
SET TARGET_DATABASE_LOCATION=%LOCATION%\database_%TARGET_CA_NAME%.txt
SET TARGET_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%TARGET_CA_NAME%.txt
SET TARGET_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%TARGET_CA_NAME%.txt

REM remove files to create when they already exist
if exist "%TARGET_CA_PRIVATE_KEY_LOCATION%" del "%TARGET_CA_PRIVATE_KEY_LOCATION%"
if exist "%TARGET_CA_REQUEST_LOCATION%" del "%TARGET_CA_REQUEST_LOCATION%"
if exist "%TARGET_CA_DER_CERTIFICATE_LOCATION%" del "%TARGET_CA_DER_CERTIFICATE_LOCATION%"
if exist "%TARGET_CA_PEM_CERTIFICATE_LOCATION%" del "%TARGET_CA_PEM_CERTIFICATE_LOCATION%"
if exist "%TARGET_DATABASE_LOCATION%" del "%TARGET_DATABASE_LOCATION%"*
if exist "%TARGET_SERIAL_NUMBER_LOCATION%" del "%TARGET_SERIAL_NUMBER_LOCATION%"*
if exist "%TARGET_CRL_NUMBER_LOCATION%" del "%TARGET_CRL_NUMBER_LOCATION%"*

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

REM Create CA related files
REM Generate an empty file
echo. 2> "%TARGET_DATABASE_LOCATION%"
echo 00 > "%TARGET_SERIAL_NUMBER_LOCATION%"
echo 00 > "%TARGET_CRL_NUMBER_LOCATION%"


REM Generate private key
"%BATCH_DIR%openssl%X64%" genrsa -des3 -out "%TARGET_CA_PRIVATE_KEY_LOCATION%" -passout pass:pass 2048 
REM Generate request key
"%BATCH_DIR%openssl%X64%" req -config "%CONFIGURATION%" -new -key "%TARGET_CA_PRIVATE_KEY_LOCATION%" -outform PEM -out "%TARGET_CA_REQUEST_LOCATION%" -passin pass:pass
REM Sign request, certificate in PEM format will be generated
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -batch -days 3650 -in "%TARGET_CA_REQUEST_LOCATION%" -extensions v3_ca -out "%TARGET_CA_PEM_CERTIFICATE_LOCATION%" -subj "%SUBJ%" -passin pass:pass -cert "%SIGNING_CA_PEM_CERTIFICATE_LOCATION%" -keyfile "%SIGNING_CA_PRIVATE_KEY_LOCATION%"
REM Convert PEM certificate to DER format
"%BATCH_DIR%openssl%X64%" x509 -inform PEM -in "%TARGET_CA_PEM_CERTIFICATE_LOCATION%" -outform DER -out "%TARGET_CA_DER_CERTIFICATE_LOCATION%"

GOTO END

:NO_PATH
echo No path has been specified
GOTO USAGE

:NO_SIGNING_CA
echo No signing CA has been specified
GOTO USAGE

:NO_TARGET_CA
echo No target CA has been specified
GOTO USAGE

:NO_SUBJ
echo No subject has been specified
GOTO USAGE

:USAGE
echo Usage create_intermediate_ca_cert.bat 'location_root' 'signing_ca_name' 'target_ca_name' 'subj'
echo where signing_ca_name and target_ca_name are the filename without file extention (e.g. myCA)
echo and subj has the form: "/C=%COUNTRY%/L=%LOCATION%/ST=%STATE%/O=%ORGANIZATION%/OU=%ORGANIZATION_UNIT%/CN=%TO%"
goto END

:END

