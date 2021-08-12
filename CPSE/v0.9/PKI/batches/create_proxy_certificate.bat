@echo off

SETLOCAL
SET BATCH_DIR=%~dp0

if %1.==. GOTO NO_PATH_CERTIFICATE_REQUEST
if %2.==. GOTO NO_NAME_CERTIFICATE_REQUEST
if %3.==. GOTO NO_SUBJ_CERTIFICATE_REQUEST

SET LOCATION=%~1
SET SIGNING_NAME=%~2
SET SUBJ=%~3
echo Subject: %~3

SET CONFIGURATION=%BATCH_DIR%openssl.cnf
SET PROXY_NAME=%SIGNING_NAME%_proxy

SET SIGNING_PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%SIGNING_NAME%.pem
SET SIGNING_TEMP_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%SIGNING_NAME%.pem
SET SIGNING_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%SIGNING_NAME%.der
REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_%SIGNING_NAME%.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_%SIGNING_NAME%.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_%SIGNING_NAME%.txt

SET PROXY_PRIVATE_KEY_LOCATION=%LOCATION%\private\private_key_%PROXY_NAME%.pem
SET PROXY_TEMP_PEM_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%PROXY_NAME%_part.pem
SET PROXY_TEMP_DER_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%PROXY_NAME%_part.der
SET PROXY_CERTIFICATE_LOCATION=%LOCATION%\certs\cert_%PROXY_NAME%.der
SET PROXY_REQUEST_LOCATION=%LOCATION%\request\req_%PROXY_NAME%.csr

REM remove files to create when they already exist
if exist "%PROXY_PRIVATE_KEY_LOCATION%" del "%PROXY_PRIVATE_KEY_LOCATION%"
if exist "%PROXY_TEMP_PEM_CERTIFICATE_LOCATION%" del "%PROXY_TEMP_PEM_CERTIFICATE_LOCATION%"
if exist "%PROXY_TEMP_DER_CERTIFICATE_LOCATION%" del "%PROXY_TEMP_DER_CERTIFICATE_LOCATION%"
if exist "%PROXY_CERTIFICATE_LOCATION%" del "%PROXY_CERTIFICATE_LOCATION%"
if exist "%PROXY_REQUEST_LOCATION%" del "%PROXY_REQUEST_LOCATION%"

if not exist "%LOCATION%" md "%LOCATION%"
if not exist "%LOCATION%\private" md "%LOCATION%\private"
if not exist "%LOCATION%\certs" md "%LOCATION%\certs"
if not exist "%LOCATION%\crl" md "%LOCATION%\crl"
if not exist "%LOCATION%\request" md "%LOCATION%\request"

REM not self signed certificates usually don't have a data base and a serial number file. Create them
if not exist "%CA_DATABASE_LOCATION%" (
	REM Generate an empty file
	echo. 2> "%CA_DATABASE_LOCATION%"
)
if not exist "%CA_SERIAL_NUMBER_LOCATION%" (
	echo 00 > "%CA_SERIAL_NUMBER_LOCATION%"
)

REM Generate private key
"%BATCH_DIR%openssl%X64%" genrsa -des3 -out "%PROXY_PRIVATE_KEY_LOCATION%"  -passout pass:pass 2048 
REM Generate request key
"%BATCH_DIR%openssl%X64%" req -config "%CONFIGURATION%" -new -key "%PROXY_PRIVATE_KEY_LOCATION%" -outform PEM -out "%PROXY_REQUEST_LOCATION%" -passin pass:pass
REM Sign request, certificate in PEM format will be generated
"%BATCH_DIR%openssl%X64%" ca -config "%CONFIGURATION%" -batch -days 365 -in "%PROXY_REQUEST_LOCATION%" -extensions v3_proxy -out "%PROXY_TEMP_PEM_CERTIFICATE_LOCATION%" -subj "%SUBJ%" -passin pass:pass -cert "%SIGNING_TEMP_CERTIFICATE_LOCATION%" -keyfile "%SIGNING_PRIVATE_KEY_LOCATION%"
REM Convert PEM certificate to DER format
"%BATCH_DIR%openssl%X64%" x509 -inform PEM -in "%PROXY_TEMP_PEM_CERTIFICATE_LOCATION%" -outform DER -out "%PROXY_TEMP_DER_CERTIFICATE_LOCATION%"
REM Attach the issuer certificate for chain validation
type "%PROXY_TEMP_DER_CERTIFICATE_LOCATION%" "%SIGNING_CERTIFICATE_LOCATION%" > "%PROXY_CERTIFICATE_LOCATION%"
REM remove temporary partial certificate
del %PROXY_TEMP_DER_CERTIFICATE_LOCATION%
GOTO END_CERTIFICATE_REQUEST

:NO_PATH_CERTIFICATE_REQUEST
echo No path has been specified
GOTO USAGE

:NO_NAME_CERTIFICATE_REQUEST
echo No name has been specified
GOTO USAGE

:NO_SUBJ_CERTIFICATE_REQUEST
echo No subject has been specified
GOTO USAGE

:USAGE
echo Usage create_proxy_certificate.bat 'location_root' 'signing_certificate_name' 'subj'
echo where signing_certificate_name is the filename without file extention (e.g. client2)
echo and subj has the form: "/C=%COUNTRY%/DC=%DomainComponent%/ST=%STATE%/O=%ORGANIZATION%/OU=%ORGANIZATION_UNIT%/CN=%TO%"
GOTO END_CERTIFICATE_REQUEST

:END_CERTIFICATE_REQUEST
