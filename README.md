# OPCUA-information-model-validation-adapter
# IPK Fraunhofer Berlin

# Description to the Deviation Analyser
The Deviation Analyser is a tool that shows the output of the Validation Adapter and filters and displays erros and deviations between the information models that were read in by the Validation Adapter and the Emulation Server.

# Description to the Validation Adapter
The Validation Adapter was build to read in information models through their node structure and tests the read in information models against information models from server side.
It is able to detect deviations and unconventional implementations of information models. It also outputs the information model that was read in or implemented on server side.

# Description to the Emulation Server
We build an Emulation Server at IPK that is able to freely read in information models and also detect simple unconveniant implemantations like missing node ids. Through the usage of licensed software it is not possible to publish the Emulation Server open source.

# Open Source Emulation Server solutions
We tested and configured different open source Emulation Servers from the Unified Automation which contain hard coded information models which can be tested against by the Validation Adapter and is an output product.
