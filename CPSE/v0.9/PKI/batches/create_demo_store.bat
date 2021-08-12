@echo off
SETLOCAL ENABLEDELAYEDEXPANSION
set BATCH_DIR=%~dp0

set DEFAULT_STORE_PATH=%BATCH_DIR%..\all_certificates
set OPENSSL_CONF=%BATCH_DIR%openssl.cnf
set RANDFILE=%DEFAULT_STORE_PATH%\.rnd

rem Parse batch parameters
set INTERACTIVE=true

set HOSTNAME=
set DOMAINNAME=
set IPADDR=
set STORE_PATH=%DEFAULT_STORE_PATH%

:param_loop
if "%1"=="" goto end_param_loop

if "%1"=="-a" (
	set INTERACTIVE=auto
	shift
	goto param_loop
)

if "%1"=="-c" (
	if "%4"=="" goto usage

	set INTERACTIVE=false

	set HOSTNAME=%~2
	set DOMAINNAME=%~3
	set IPADDR=%~4
	
	shift & shift & shift & shift
	goto param_loop
)

if "%1"=="-o" (
	set STORE_PATH=%~2
	shift & shift
	goto param_loop
)
goto usage

:end_param_loop

if "%INTERACTIVE%"=="false"  goto ip_selected
rem -------------------------------
rem  Non-interactive configuration
rem -------------------------------

rem Host name
for /f %%i in ('HOSTNAME') do SET HOSTNAME=%%i

rem Domain
SET DOMAINNAME=%USERDNSDOMAIN%

rem IP address
set IPS=
for /f %%l in ('"%BATCH_DIR%\PrintIpAddresses.exe"') do (
set IPS=!IPS! %%l
)

set /a IPCOUNT=0
for %%i in (%IPS%) do (
set /a IPCOUNT=!IPCOUNT! + 1
echo !IPCOUNT!: %%i
)

if %IPCOUNT% EQU 0 (
set IPADDR=127.0.0.1
if "%INTERACTIVE%"=="auto" goto ip_selected
echo No IP address found, using 127.0.0.1 ^(localhost^)
echo.
pause
goto ip_selected
)

if %IPCOUNT% EQU 1 (
for %%i in (%IPS%) do set IPADDR=%%i
goto ip_selected
)

set /a SELECTED=0
if "%INTERACTIVE%"=="auto" goto selection_ok

:select_IP
set /P INPUT=Please select an IP address (x to abort):
if /i "%INPUT%" == "x" goto done
set /a SELECTED=%INPUT%

if %INPUT% GTR %IPCOUNT% goto input_error
if %INPUT% LSS 1 goto input_error
goto selection_ok

:input_error
rem invalid selection - show possible options again
echo.
echo Invalid selection
echo.
set /a IPCOUNT=0
for %%i in (%IPS%) do (
set /a IPCOUNT=!IPCOUNT! + 1
echo !IPCOUNT!: %%i
)
goto select_IP

:selection_ok
set /a SEL=0
for %%i in (%IPS%) do (
set /a SEL=!SEL! + 1
if !SEL! EQU %SELECTED% set IPADDR=%%i
)

:ip_selected
if "%IPADDR%" == "" goto error_no_ip

if "%INTERACTIVE%"=="false" goto info_done
if "%INTERACTIVE%"=="auto" goto info_done
echo Creating certificates with following information:
echo    Host: "%HOSTNAME%"
echo    Domain: "%DOMAINNAME%"
echo    IP Address: "%IPADDR%"
echo.
echo If anything is incorrect please abort using Ctrl-C
pause
:info_done

if not exist "%STORE_PATH%" md "%STORE_PATH%"

rem Do not include the domain name if the host does not belong to a domain
if not "%DOMAINNAME%" == "" (
	set FQDN=%HOSTNAME%.%DOMAINNAME%
) else (
	set FQDN=%HOSTNAME%
)

SET SERVER_ALTERNATIVE_SUBJECT=URI:urn:%HOSTNAME%/Softing/OpcUa/TestServer,DNS:%FQDN%,IP:%IPADDR%
SET CLIENT_ALTERNATIVE_SUBJECT=URI:urn:%HOSTNAME%/Softing/OpcUa/TestClient

rem separate subject for sample applications
SET SERVER_SAMPLE_SUBJECT=URI:urn:%HOSTNAME%/Softing/OpcUa/SampleServer,DNS:%FQDN%,IP:%IPADDR%
SET CLIENT_SAMPLE_SUBJECT=URI:urn:%HOSTNAME%/Softing/OpcUa/SampleClient

rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%SERVER_ALTERNATIVE_SUBJECT%

REM These three variables are used by the OpenSSL as environment variables
SET CA_DATABASE_LOCATION=%LOCATION%\database_default.txt
SET CA_SERIAL_NUMBER_LOCATION=%LOCATION%\serial_default.txt
SET CA_CRL_NUMBER_LOCATION=%LOCATION%\crlnumber_default.txt

set X64=_x64
"%BATCH_DIR%openssl%X64%" version >NUL: 2>&1
if errorlevel 1 set X64=

"%BATCH_DIR%openssl%X64%" version

echo =====================================
echo = Creating root CA
echo =====================================
CALL:setSubject "Softing OpcUa Test CA" "%HOSTNAME%"
CALL "%BATCH_DIR%create_root_ca_cert.bat" "%STORE_PATH%" root_ca "%X509_SUBJ%"

echo =====================================
echo = Creating intermediate CA
echo =====================================
CALL:setSubject "Softing Demo Intermediate CA" "%HOSTNAME%"
CALL "%BATCH_DIR%create_intermediate_ca_cert.bat" "%STORE_PATH%" root_ca inter_ca "%X509_SUBJ%"


echo =====================================
echo = Creating server certificate
echo =====================================
CALL:setSubject "Softing Test Server" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca server "%X509_SUBJ%"

echo =====================================
echo = Creating second server certificate
echo =====================================
CALL:setSubject "Softing Test Server2" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca server2 "%X509_SUBJ%"

echo =====================================
echo = Revoking second server certificate
echo =====================================
CALL "%BATCH_DIR%revoke_certificate.bat" "%STORE_PATH%" inter_ca server2

rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%CLIENT_ALTERNATIVE_SUBJECT%

echo =====================================
echo = Creating client certificate
echo =====================================
CALL:setSubject "Softing Test Client" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca client "%X509_SUBJ%"

echo =====================================
echo = Creating proxy certificate of client certificate
echo =====================================
CALL:setSubject "Softing Test Client Proxy" "%HOSTNAME%"
CALL "%BATCH_DIR%create_proxy_certificate.bat" "%STORE_PATH%" client "%X509_SUBJ%"

echo =====================================
echo = Creating second client certificate
echo =====================================
CALL:setSubject "Softing Test Client2" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca client2 "%X509_SUBJ%"

echo =====================================
echo = Revoking second client certificate
echo =====================================
CALL "%BATCH_DIR%revoke_certificate.bat" "%STORE_PATH%" inter_ca client2

echo =====================================
echo = Updating CRL of intermediate CA
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" inter_ca


rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%SERVER_ALTERNATIVE_SUBJECT%

echo =====================================
echo = Creating second intermediate CA
echo =====================================
CALL:setSubject "Softing Demo Intermediate CA 2" "%HOSTNAME%"
CALL "%BATCH_DIR%create_intermediate_ca_cert.bat" "%STORE_PATH%" root_ca inter_ca2 "%X509_SUBJ%"

echo =====================================
echo = Revoking second intermediate CA
echo =====================================
CALL "%BATCH_DIR%revoke_certificate.bat" "%STORE_PATH%" root_ca inter_ca2

echo =====================================
echo = Creating third server certificate
echo =====================================
CALL:setSubject "Softing Test Server3" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca2 server3 "%X509_SUBJ%"

echo =====================================
echo = Creating fourth server certificate
echo =====================================
CALL:setSubject "Softing Test Server4" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca2 server4 "%X509_SUBJ%"

echo =====================================
echo = Revoking fourth server certificate
echo =====================================
CALL "%BATCH_DIR%revoke_certificate.bat" "%STORE_PATH%" inter_ca2 server4

rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%CLIENT_ALTERNATIVE_SUBJECT%

echo =====================================
echo = Creating third client certificate
echo =====================================
CALL:setSubject "Softing Test Client3" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca2 client3 "%X509_SUBJ%"

echo =====================================
echo = Creating proxy certificate of third client certificate
echo =====================================
CALL:setSubject "Softing Test Client3 Proxy" "%HOSTNAME%"
CALL "%BATCH_DIR%create_proxy_certificate.bat" "%STORE_PATH%" client3 "%X509_SUBJ%"

echo =====================================
echo = Creating fourth client certificate
echo =====================================
CALL:setSubject "Softing Test Client4" "%HOSTNAME%"
CALL "%BATCH_DIR%create_certificate.bat" "%STORE_PATH%" inter_ca2 client4 "%X509_SUBJ%"

echo =====================================
echo = Revoking fourth client certificate
echo =====================================
CALL "%BATCH_DIR%revoke_certificate.bat" "%STORE_PATH%" inter_ca2 client4

echo =====================================
echo = Updating CRL of revoked intermediate CA
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" inter_ca2


echo =====================================
echo = Updating CRL of root CA
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" root_ca


rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%SERVER_ALTERNATIVE_SUBJECT%

echo =====================================
echo = Generating server self signed certificate
echo =====================================
CALL:setSubject "Softing OpcUa Test Server (self signed)" "%HOSTNAME%"
CALL "%BATCH_DIR%create_self_signed_certificate.bat" "%STORE_PATH%" server_self_signed "%X509_SUBJ%"

echo =====================================
echo = Updating CRL of server self signed
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" server_self_signed

rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%CLIENT_ALTERNATIVE_SUBJECT%

echo =====================================
echo = Generating client self signed certificate
echo =====================================
CALL:setSubject "Softing OpcUa Test Client (self signed)" "%HOSTNAME%"
CALL "%BATCH_DIR%create_self_signed_certificate.bat" "%STORE_PATH%" client_self_signed "%X509_SUBJ%"

echo =====================================
echo = Updating CRL of client self signed
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" client_self_signed

rem create certificates for sample
rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%CLIENT_SAMPLE_SUBJECT%

echo =====================================
echo = Generating sample client certificate
echo =====================================
CALL:setSubject "Softing OpcUa Sample Client" "%HOSTNAME%"
CALL "%BATCH_DIR%create_self_signed_certificate.bat" "%STORE_PATH%" sample_client "%X509_SUBJ%"

echo =====================================
echo = Updating CRL of sample client
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" sample_client

rem This variable is required to be set because it is referenced from the OpenSSL config file.
SET SOFTING_CERT_GENERATION_URI=%SERVER_SAMPLE_SUBJECT%

echo =====================================
echo = Generating sample server certificate
echo =====================================
CALL:setSubject "Softing OpcUa Sample Server" "%HOSTNAME%"
CALL "%BATCH_DIR%create_self_signed_certificate.bat" "%STORE_PATH%" sample_server "%X509_SUBJ%"

echo =====================================
echo = Updating CRL of sample server
echo =====================================
CALL "%BATCH_DIR%update_revokation_list.bat" "%STORE_PATH%" sample_server

echo =====================================
echo = Create DH parameters for the server
echo =====================================
CALL "%BATCH_DIR%create_dhparam_file.bat" "%STORE_PATH%"

rem Now create the PKI stores for test client and server
set CLIENT_STORE_PATH="%DEFAULT_STORE_PATH%\..\test_client"
if NOT exist %CLIENT_STORE_PATH% mkdir %CLIENT_STORE_PATH%
if NOT exist %CLIENT_STORE_PATH%\trusted mkdir %CLIENT_STORE_PATH%\trusted
if NOT exist %CLIENT_STORE_PATH%\own mkdir %CLIENT_STORE_PATH%\own
if NOT exist %CLIENT_STORE_PATH%\issuer mkdir %CLIENT_STORE_PATH%\issuer
if NOT exist %CLIENT_STORE_PATH%\rejected mkdir %CLIENT_STORE_PATH%\rejected
if NOT exist %CLIENT_STORE_PATH%\crl mkdir %CLIENT_STORE_PATH%\crl
set SERVER_STORE_PATH="%DEFAULT_STORE_PATH%\..\test_server"
if NOT exist %SERVER_STORE_PATH% mkdir %SERVER_STORE_PATH%
if NOT exist %SERVER_STORE_PATH%\trusted mkdir %SERVER_STORE_PATH%\trusted
if NOT exist %SERVER_STORE_PATH%\own mkdir %SERVER_STORE_PATH%\own
if NOT exist %SERVER_STORE_PATH%\issuer mkdir %SERVER_STORE_PATH%\issuer
if NOT exist %SERVER_STORE_PATH%\rejected mkdir %SERVER_STORE_PATH%\rejected
if NOT exist %SERVER_STORE_PATH%\crl mkdir %SERVER_STORE_PATH%\crl

copy /y "%DEFAULT_STORE_PATH%\certs\cert_server_self_signed.der" %CLIENT_STORE_PATH%\trusted\cert_server_self_signed.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\certs\cert_client_self_signed.der" %CLIENT_STORE_PATH%\own\cert_client_self_signed.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\private\private_key_client_self_signed.pem" %CLIENT_STORE_PATH%\own\private_key_client_self_signed.pem > NUL:

copy /y "%DEFAULT_STORE_PATH%\certs\cert_client_self_signed.der" %SERVER_STORE_PATH%\trusted\cert_client_self_signed.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\certs\cert_server_self_signed.der" %SERVER_STORE_PATH%\own\cert_server_self_signed.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\private\private_key_server_self_signed.pem" %SERVER_STORE_PATH%\own\private_key_server_self_signed.pem > NUL:
copy /y "%DEFAULT_STORE_PATH%\private\dhparam.pem" %SERVER_STORE_PATH%\own\dhparam.pem > NUL:

rem Now create the PKI stores for sample client and server
set CLIENT_STORE_PATH="%DEFAULT_STORE_PATH%\..\sample_client"
if NOT exist %CLIENT_STORE_PATH% mkdir %CLIENT_STORE_PATH%
if NOT exist %CLIENT_STORE_PATH%\trusted mkdir %CLIENT_STORE_PATH%\trusted
if NOT exist %CLIENT_STORE_PATH%\own mkdir %CLIENT_STORE_PATH%\own
if NOT exist %CLIENT_STORE_PATH%\issuer mkdir %CLIENT_STORE_PATH%\issuer
if NOT exist %CLIENT_STORE_PATH%\crl mkdir %CLIENT_STORE_PATH%\crl
set SERVER_STORE_PATH="%DEFAULT_STORE_PATH%\..\sample_server"
if NOT exist %SERVER_STORE_PATH% mkdir %SERVER_STORE_PATH%
if NOT exist %SERVER_STORE_PATH%\trusted mkdir %SERVER_STORE_PATH%\trusted
if NOT exist %SERVER_STORE_PATH%\own mkdir %SERVER_STORE_PATH%\own
if NOT exist %SERVER_STORE_PATH%\issuer mkdir %SERVER_STORE_PATH%\issuer
if NOT exist %SERVER_STORE_PATH%\rejected mkdir %SERVER_STORE_PATH%\rejected
if NOT exist %SERVER_STORE_PATH%\crl mkdir %SERVER_STORE_PATH%\crl

copy /y "%DEFAULT_STORE_PATH%\certs\cert_sample_server.der" %CLIENT_STORE_PATH%\trusted\cert_sample_server.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\certs\cert_sample_client.der" %CLIENT_STORE_PATH%\own\cert_sample_client.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\private\private_key_sample_client.pem" %CLIENT_STORE_PATH%\own\private_key_sample_client.pem > NUL:

copy /y "%DEFAULT_STORE_PATH%\certs\cert_sample_client.der" %SERVER_STORE_PATH%\trusted\cert_sample_client.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\certs\cert_sample_server.der" %SERVER_STORE_PATH%\own\cert_sample_server.der > NUL:
copy /y "%DEFAULT_STORE_PATH%\private\private_key_sample_server.pem" %SERVER_STORE_PATH%\own\private_key_sample_server.pem > NUL:

rem Clean-up intermediate files
del /q "%DEFAULT_STORE_PATH%\*"
del /q "%DEFAULT_STORE_PATH%\certs\*.pem"
rmdir /s /q "%DEFAULT_STORE_PATH%\request"

goto done

:error_no_ip
echo.
echo Cannot determine IP address automatically.
echo Please invoke this batch using parameters to specify IP address.
echo.
goto usage

:usage
echo off
echo This batch file creates certificates and corresponding private keys for
echo the sample client and server applications delivered with the OPC UA Toolkit.
echo.
echo Usage: %~n0 ^[-a^|-c ^<Host name^> ^<Domain^> ^<IP address^>^] ^[-o ^<Output directory^>^] ^]
echo.
echo -a  Use the default values to guess the host name, domain name
echo     and IP address to create instance certificates.
echo.
echo -c  Use the given host name, domain name and IP address
echo     to create instance certificates. If this parameter is
echo     not specified, the configuration is performed interactively.
echo.
echo -o  Specifies the output directory of the generated files.
echo     By default, the generated files are placed in
echo     %DEFAULT_STORE_PATH%
goto done

rem Generates the subject using the Common Name and the Hostname and stores the result in 'X509_SUBJ'
rem call:setSubject 'Common Name' 'Hostname'
:setSubject
SET X509_SUBJ=/C=DE/L=Haar (Munich)/ST=Bayern/O=Softing IA GmbH/OU=IA/CN=%~1/emailAddress=support.automation@softing.com/DC=%~2
GOTO:EOF

:done
