@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_CERTIFICATE_REQUEST
if %2.==. GOTO NO_SIGNING_CA_NAME
if %3.==. GOTO NO_NAME_CERTIFICATE_REQUEST
if %4.==. GOTO NO_SUBJ_CERTIFICATE_REQUEST

SET LOCATION=%~1
SET SIGNING_CA_NAME=%~2
SET NAME=%~3
SET SUBJ=%~4
echo Subject: %~4

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET SIGNING_CA_PEM_CERTIFICATE=%LOCATION%\certs\cert_%SIGNING_CA_NAME%.pem
SET SIGNING_CA_PRIVATE_KEY=%LOCATION%\private\private_key_%SIGNING_CA_NAME%.pem
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%SIGNING_CA_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%SIGNING_CA_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%SIGNING_CA_NAME%.txt

SET PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%NAME%.pem
SET CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.der
SET TEMP_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%NAME%.pem
SET REQUEST_LOCATION=%LOCATION%\request\req_%NAME%.csr
REM These three locations are only needed for proxy certificates, and aren't generated here
SET CERT_DATABASE_LOCATION=%LOCATION%\database_%NAME%.txt
SET CERT_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%NAME%.txt
SET CERT_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%NAME%.txt

REM remove files to create when they already exist
if exist "%PRIVATE_KEY_LOCATION%" del "%PRIVATE_KEY_LOCATION%"
if exist "%CERTIFICATE_LOCATION%" del "%CERTIFICATE_LOCATION%"
if exist "%TEMP_CERTIFICATE_LOCATION%" del "%TEMP_CERTIFICATE_LOCATION%"
if exist "%REQUEST_LOCATION%" del "%REQUEST_LOCATION%"
if exist "%CERT_DATABASE_LOCATION%" del "%CERT_DATABASE_LOCATION%"*
if exist "%CERT_SERIAL_NUMBER_LOCATION%" del "%CERT_SERIAL_NUMBER_LOCATION%"*
if exist "%CERT_CRL_NUMBER_LOCATION%" del "%CERT_CRL_NUMBER_LOCATION%"*

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

REM Generate private key
"%BATCH_DIR%openssl%X64%" genrsa -des3 -out "%PRIVATE_KEY_LOCATION%" -passout pass:pass 2048
REM Generate request key
"%BATCH_DIR%openssl%X64%" req -config "%CONFIGURATION%" -new -key "%PRIVATE_KEY_LOCATION%" -outform PEM -out "%REQUEST_LOCATION%" -passin pass:pass
REM Sign request, certificate in PEM format will be generated
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -batch -days 365 -in "%REQUEST_LOCATION%" -out "%TEMP_CERTIFICATE_LOCATION%" -subj "%SUBJ%" -passin pass:pass -cert "%SIGNING_CA_PEM_CERTIFICATE%" -keyfile "%SIGNING_CA_PRIVATE_KEY%"
REM Convert PEM certificate to DER format
"%BATCH_DIR%openssl%X64%" x509 -inform PEM -in "%TEMP_CERTIFICATE_LOCATION%" -outform DER -out "%CERTIFICATE_LOCATION%"

GOTO END_CERTIFICATE_REQUEST

:NO_PATH_CERTIFICATE_REQUEST
echo No path has been specified
GOTO USAGE

:NO_SIGNING_CA_NAME
echo No signing CA name has been specified
GOTO USAGE

:NO_NAME_CERTIFICATE_REQUEST
echo No name has been specified
GOTO USAGE

:NO_SUBJ_CERTIFICATE_REQUEST
echo No subject has been specified
GOTO USAGE

:USAGE
echo Usage create_certificate.bat 'location_root' 'signing_ca_name' 'unique_name' 'subj'
echo where signing_ca_name is the filename without file extention (e.g. myCA)
echo and subj has the form: "/C=%COUNTRY%/DC=%DomainComponent%/ST=%STATE%/O=%ORGANIZATION%/OU=%ORGANIZATION_UNIT%/CN=%TO%"
GOTO END_CERTIFICATE_REQUEST

:END_CERTIFICATE_REQUEST

