# OPCUA informationmodel ValidationAdapter
# IPK Fraunhofer Berlin

# Description for the Validation Adapter
The Cyper Physical System Validation Adapter (CPS VA) is a software to validate communication interfaces, here especially a server, in OPC UA against a specification. For this purpose the CPS VA is configured with the help of two configuration files "AdapterConfig.properties" and "TestConfiguration.xml".

## AdapterConfig.properties
AdapterConfig.properties: This configuration file allows you to configure the CPS VA in terms of language, specification selection and location of files written by the CPS VA.

## TestConfiguration.xml
With the TestConfiguration.xml the test can be configured. OPC UA uses the internet protocol, which is why a connection can only be established using an IP address.

## ValidationAdapter.jar
After the configuration for the next test run has been made via the AdapterConfig.properties and TestConfiguration.xml The validation is performed by executing the ValidationAdapter.jar.

## Evaluation and results
A validation process is completed with the creation of three files. 
- The "LogFile.json" is a file that contains information about the validation process.
- The "LogFile + Timestamp.xml" contains information about the results of the validation. 
- And a file that represents the structure of the data in the server in an OPC UA specific format (output name can be configured in the "TestConfiguration.xml" via testsystem). 

# Description for the Deviation Analyser
The Deviation Analyser is a tool that shows the output of the Validation Adapter and filters and displays erros and deviations between the information models that were read in by the Validation Adapter and the Emulation Server. The tool will automatically open after completely running the Validation Adapter.

# Description for the Emulation Server
We build an Emulation Server at IPK that is able to freely read in information models and also detect simple unconveniant implemantations like missing node ids. Through the usage of licensed software it is not possible to publish the Emulation Server open source.

# Open Source Emulation Server solutions
We tested and configured different open source Emulation Servers from the Unified Automation which contain hard coded information models which can be tested against by the Validation Adapter and is an output product.

## Starting the Open Source Emulation Server solution: UAServerCPP/UAAnsiCServer
After downloading and installing one of the open source servers run the "uaservercpp.exe" or "uaserverc.exe" under the installed folder "\UaCPPServer\bin" or "\UaAnsiCServer\bin". A new window will show up which already opens the server under the stated URL: "opc.tcp://[PC/Machine-Name]:[Port-Number]". These two informations are necessary for the configuration of the "TestConfiguration.xml"-file from the ValidationAdapter. The "PC/Machine-Name" of your PC/Machine will be the ip and the "Port-Number" will be the port for the "TestConfiguration.xml"-file. 
  
 ## Configuration of the Open Source Emulation Server solutions: UAServerCPP
You can also configure the UAServerCPP via the "ServerConfig.xml" which can be found under "\UaCPPServer\bin" where you also found the "uaservercpp.exe" to start the server or use the "admindialog.exe" under "\UaCPPServer\bin\admindialog" to configure the server ip and port through a graphical surface. If you use the graphical surface you can change the network to local/localhost choosing "Local Only" instead "All" under the Network Adapter. The "PC/Machine-Name" of your PC/Machine which will be the ip to connect to can be changed under Hostname/IP and the Port can be changed too but please be aware that the port needs to be free, meaning no service or program already uses this port.
  
# Link to the Open Source Emulation Server solutions
As already stated you can find open source server solutions for example from the Unified Automation which can be freely configured. Every Server also contains a detailed description how to configure and use them. Please notice you have to register at Unified Automation free of charge.
Link: https://www.unified-automation.com/downloads/opc-ua-servers.html

