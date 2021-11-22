/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file gives the rough outline for validation process.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import databases.TestConfiguration;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANode;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;
import externalFiles.TestConfigManagement;
import logfilemanagement.LogFileManagement;
import logfilemanagement.LoggingOutputStream;

public class Validation {
	
	private static Logger logger = LogManager.getLogger(Validation.class);

	public static void initializeLogFileManagement(String location) {
		
		LogFileManagement.setLocation(location);

		// Creates an output stream an redirect every print to console into the logger
		System.setErr(new PrintStream(new LoggingOutputStream("info")));
	}

	public static String getLogFileName() {
		return LogFileManagement.getLogFileName();
	}

	public static String addErrorCode(String errorCode) {
		return LogFileManagement.addErrorCode(errorCode);
	}

	public static void startValidation(String language, String testConfiguration, String xmlFile, String xmllogPath) {
		
		TestConfiguration testConfig = TestConfigManagement.getTestConfig(testConfiguration);
		logger.info(LogFileManagement.addErrorCode("Info002") + testConfiguration);
		String serverUrl = testConfig.getIp();
		String port = testConfig.getPort();
		String securityPolicy = testConfig.getSecurity();
		if(!testConfig.getFramework().equals("")) {
			OpcUaManagement.setFramework(testConfig.getFramework());
		} else {
			logger.info(LogFileManagement.addErrorCode("Error013"));
			JOptionPane.showMessageDialog(null, "Error, framework is empty. Eclipse Milo is selected as default");
		}
		
		if(!serverUrl.equals("") && !port.equals("") && !securityPolicy.equals("")) {
			
			logger.info(LogFileManagement.addErrorCode("Info017")  + serverUrl);
			logger.info(LogFileManagement.addErrorCode("Info018") + port);
			logger.info(LogFileManagement.addErrorCode("Info019") + securityPolicy);

			if (OpcUaManagement.createClient(serverUrl, port, securityPolicy)) {

				JOptionPane.showMessageDialog(null, "OPC UA connection created. Press enter to continue");

				if (OpcUaManagement.connect()) {
					
					logger.info(LogFileManagement.addErrorCode("Info003") + LogFileManagement.addErrorCode("Info022"));
					
					JOptionPane.showMessageDialog(null, "OPC UA connection established. Press enter to continue");
					logger.info(LogFileManagement.addErrorCode("Info014") + LogFileManagement.addErrorCode("Info006"));

					JOptionPane.showMessageDialog(null, "Read Informationmodell from server. Press enter to continue");
					OpcUaManagement.createServerInformationmodel();
					UANodeSet serverInformationModel = OpcUaManagement.getServerInformationmodel();

					logger.info("Info020" + xmlFile);
					UANodeSet xmlInformationModel = OpcUaManagement.loadXmlInformationModel(xmlFile);

					if (serverInformationModel != null) {

						if (xmlInformationModel != null) {
							
							logger.info(LogFileManagement.addErrorCode("Info007"));
							JOptionPane.showMessageDialog(null,	"Comparing nodes from spec. to nodes fom server. Press enter to continue");

							// Compare Aliases
							// Not able to get aliases from server

							// Compare Server Uris
							// Not able to get uris from server

							// Compare NamespaceUris
							// Not able to get NameSpaceUris from server

							// Compare Models
							// Not able to get Models from server

							// Compare Nodes

							if(xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().size() > 0) {
								
								int sizeOfXmlNodes = xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().size();
								
								for (int i = 0; i < sizeOfXmlNodes; i++) {
									
									UANode xmlNode = xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().get(i);
									
									String xmlNodeId = "";
									if(xmlNode.getNodeId() != null) {
										xmlNodeId = xmlNode.getNodeId();
									}

									String xmlBrowseName = "";
									if(xmlNode.getBrowseName() != null) {
										xmlBrowseName = xmlNode.getBrowseName();
									}

									List<UANode> serverNodes = new ArrayList<UANode>();
									if(!xmlNodeId.equals("") || !xmlBrowseName.equals("")) {
										serverNodes = OpcUaManagement.findNode(xmlNodeId, xmlBrowseName, serverInformationModel);
									} else {
										logger.warn("nodeId = " + xmlNodeId + ", browseName = " + xmlBrowseName + ", are empty");
									}

									if(serverNodes.size() > 0) {
										
										xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().remove(i);

										for (int j = 0; j < serverNodes.size(); j++) {
											
											UANode serverNode = serverNodes.get(j);
											
											xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().add(i, OpcUaManagement.compareNode(serverNode, xmlNode));
											
											i += 1;
											sizeOfXmlNodes = xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().size();
											
										}
										
									} else {
										if(!xmlNodeId.equals("") || !xmlBrowseName.equals("")) {
											logger.warn("nodeId = " + xmlNodeId + ", browseName = " + xmlBrowseName + ", " + LogFileManagement.addErrorCode("Bad0011"));
										}
										
									}
								}
							} else {
								logger.error("while getting nodes from the xml");
								JOptionPane.showMessageDialog(null, "Error, while getting nodes from specification.");
							}
							
							List<UANode> serverNodes = serverInformationModel.getUAObjectOrUAVariableOrUAMethod();
							
							for (int i = 0; i < serverNodes.size(); i++) {
								
								HashMap<String, String> checkedNodes = OpcUaManagement.getCheckNodes();
								
								if (!(checkedNodes.containsKey(serverNodes.get(i).getNodeId()) || checkedNodes.containsKey(serverNodes.get(i).getBrowseName()))) {
									
									logger.warn("nodeId = " + serverNodes.get(i).getNodeId() + ", browseName = " + serverNodes.get(i).getBrowseName() + LogFileManagement.addErrorCode("Bad0019"));
									
									xmlInformationModel.getUAObjectOrUAVariableOrUAMethod().add(serverNodes.get(i));
									
								}
								
							}
							
							JOptionPane.showMessageDialog(null,	"Writing informationmodel logfile. Press enter to continue");
							if(!testConfig.getTestSystem().equals("")) {
								OpcUaManagement.writeServerInformationModel(
										xmlInformationModel,
										xmllogPath + testConfig.getTestSystem() + ".xml");
							} else {
								logger.error("testsystem = empty");
								OpcUaManagement.writeServerInformationModel(xmlInformationModel,
										xmllogPath + "default" + ".xml");
							}							

						} else {
							JOptionPane.showMessageDialog(null, "Error: Specification not found. Press enter to continue");
							logger.info(LogFileManagement.addErrorCode("Error002"));
						}

					} else {

						JOptionPane.showMessageDialog(null,
								"Problem with getting informationmodel from server. (serverInformationModel = null) Press enter to continue");
						logger.info(LogFileManagement.addErrorCode("Error007"));
						
						logger.warn(LogFileManagement.addErrorCode("Warn003") + String.valueOf(OpcUaManagement.getFailures()));
					}

				} else {
					logger.info(LogFileManagement.addErrorCode("Info003") + LogFileManagement.addErrorCode("Error006"));
					JOptionPane.showMessageDialog(null, "Connection not possible. See LogFile.json for further details");
				}
				
				OpcUaManagement.disconnect();
				
			} else {
				JOptionPane.showMessageDialog(null, "Connection not possible. See LogFile.json for further details");
			}

		} else {
			logger.error("serverUrl =" + serverUrl + ", port = " + port + ", securityPolicy = " + securityPolicy);
			JOptionPane.showMessageDialog(null, "Error, serverURL, port or security is empty");
		}
		JOptionPane.showMessageDialog(null, "Writing json log file. Press enter to continue");

	}

	public static void openLogFiles(String xmllogPath) {
		LogFileManagement.openLogFiles(xmllogPath);
	}

}
