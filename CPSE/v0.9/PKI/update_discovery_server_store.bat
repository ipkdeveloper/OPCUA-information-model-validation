@echo off
set PROGRAM_DATA_FOLDER=%ALLUSERSPROFILE%

SET ALL_CERT_STORE_PATH=all_certificates
SET SERVER_STORE_PATH=test_server
SET SAMPLE_SERVER_STORE_PATH=sample_server
SET DISCOVERY_STORE_PATH=%PROGRAM_DATA_FOLDER%\OPC Foundation\UA\Discovery\pki

echo =======================================================================
echo = Update self signed server in the Opc Discovery Server store
echo =======================================================================

copy /y "%DISCOVERY_STORE_PATH%\own\ualdscert.der" "%ALL_CERT_STORE_PATH%\certs\cert_discovery_server.der"
copy /y "%DISCOVERY_STORE_PATH%\own\ualdscert.der" "%SERVER_STORE_PATH%\trusted\cert_discovery_server.der"
copy /y "%DISCOVERY_STORE_PATH%\own\ualdscert.der" "%SAMPLE_SERVER_STORE_PATH%\trusted\cert_discovery_server.der"
copy /y "%SERVER_STORE_PATH%\own\cert_server_self_signed.der" "%DISCOVERY_STORE_PATH%\trusted\certs"
copy /y "%SAMPLE_SERVER_STORE_PATH%\own\cert_sample_server.der" "%DISCOVERY_STORE_PATH%\trusted\certs"
