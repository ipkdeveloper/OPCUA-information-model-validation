#
<a href="https://www.ipk.fraunhofer.de/">
    <img src="https://www.ipk.fraunhofer.de/content/dam/ipk/IPK_Hauptseite/logos/logo-fraunhofer-ipk.png" align="right" height="60"  title="Fraunhofer IPK"/>
</a>

# OPCUA informationmodel ValidationAdapter

## Pictures

<img src="https://user-images.githubusercontent.com/83827677/149491215-196c68c5-e812-45a8-9de5-fabab7b66867.PNG" width="500" height="300">

<img src="https://user-images.githubusercontent.com/83827677/149491241-021b3b88-6eff-41a0-8877-359782e24dcc.PNG" width="500" height="300">

<img src="https://user-images.githubusercontent.com/83827677/149491255-f560f143-fd89-4c84-bb1f-79595221b354.png" width="500" height="300">


## About the project
The following project comprises of three main elements: The Validation Adapter, Deviation Analyser and an Emulation Server.

### Build with
The software tools were mainly built in the programming language Java and the Emulation Server was built in C++.

## 1. Description for the Validation Adapter
The [Cyper Physical System Validation Adapter (CPS VA)](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA) is a software to validate communication interfaces, here especially to validate the information model from a server, in [OPC UA](https://opcfoundation.org/) against a specification.

### Getting started
You can simply watch the following [instruction video](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/Instructions_video%20small.mp4) or read the complete [documentation of the Validation Adapter](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/Instructions%20for%20the%20CPS%20Validation%20Adapter.docx).

#### Prerequisites
The CPS VA is configured with the help of two configuration files ["AdapterConfig.properties"](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/AdapterConfig.properties) and ["TestConfiguration.xml"](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml) foundable in the [CPSVA](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA) folder.

1. [AdapterConfig.properties](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/AdapterConfig.properties): This configuration file allows you to configure the CPS VA in terms of language, specification selection and location of files written by the CPS VA.

2. [TestConfiguration.xml](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml): The test can be configured via this file. OPC UA uses the internet protocol, which is why a connection can only be established using an IP address.

#### Running 
After the configuration for the next test run has been made via the [AdapterConfig.properties](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/AdapterConfig.properties) and [TestConfiguration.xml](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml) The validation is performed by executing the [ValidationAdapter.jar](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/ValidationAdapter.jar).

#### Evaluation and results
A validation process is completed with the creation of three files. 
- The ["LogFile.json"](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/logs/LogFile.json) is a file that contains information about the validation process.
- The "LogFile + Timestamp.xml" contains information about the results of the validation. 
- And a file that represents the structure of the data in the server in an OPC UA specific format (output name can be configured in the ["TestConfiguration.xml"](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml) via testsystem). 

## 2. Description for the Deviation Analyser
The [Deviation Analyser](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSDA/DeviationAnalyse) is a tool that shows the output of the [Validation Adapter](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA) and filters and displays errors and deviations between the information models that were read in by the [Validation Adapter](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA) and the Emulation Server. The tool will automatically open after completely running the [Validation Adapter](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA).

## 3. Description for the Emulation Server
We build an Emulation Server at IPK that is able to freely read in information models and also detect simple inconvenient implemantations like missing node ids. Through the usage of licensed software it is not possible to publish the Emulation Server open source.

### Open Source Emulation Server solutions
We tested and configured different open source Emulation Servers from the [Unified Automation](https://www.unified-automation.com/) which contain hard coded information models which can be tested  by the Validation Adapter to detect inconvenient implemantations and also output the hard coded information models.

### Starting the Open Source Emulation Server solution: UAServerCPP/UAAnsiCServer

After downloading and installing one of the [open source servers](https://www.unified-automation.com/de/downloads/opc-ua-servers.html) run the `uaservercpp.exe` or `uaserverc.exe` under the installed folder `\UaCPPServer\bin` or `\UaAnsiCServer\bin`. A new window will show up which already opens the server under the stated URL: `opc.tcp://[PC/Machine-Name]:[Port-Number]`. These two informations are necessary for the configuration of the ["TestConfiguration.xml"-file](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml) from the [ValidationAdapter](https://github.com/ipkdeveloper/OPCUA-information-model-validation/tree/master/CPSVA). The `PC/Machine-Name` of your PC/Machine will be the ip and the `Port-Number` will be the port for the ["TestConfiguration.xml"-file](https://github.com/ipkdeveloper/OPCUA-information-model-validation/blob/master/CPSVA/TestConfiguration.xml). 
  
 ### Configuration of the Open Source Emulation Server solutions: UAServerCPP
You can also configure the [UAServerCPP](https://www.unified-automation.com/de/downloads/opc-ua-servers.html) via the `ServerConfig.xml` which can be found under `\UaCPPServer\bin` where you also found the `uaservercpp.exe` to start the server or use the `admindialog.exe` under `\UaCPPServer\bin\admindialog` to configure the server ip and port through a graphical surface. If you use the graphical surface you can change the network to local/localhost choosing `Local Only` instead `All` under the Network Adapter. The `PC/Machine-Name` of your PC/Machine which will be the ip to connect to can be changed under Hostname/IP and the Port can be changed too but please be aware that the port needs to be free, meaning no service or program already uses this port.
  
### Link to the Open Source Emulation Server solutions
As already stated you can find open source server solutions for example from the Unified Automation which can be freely configured. Every Server also contains a detailed description how to configure and use them. Please notice you have to register at Unified Automation free of charge.
Link: https://www.unified-automation.com/downloads/opc-ua-servers.html
