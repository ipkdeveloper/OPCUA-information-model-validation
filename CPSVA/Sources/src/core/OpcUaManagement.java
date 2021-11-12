/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is mainly capable of comparing opc ua nodes and calling other methods.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import databases.org.opcfoundation.ua._2011._03.uanodeset.LocalizedText;
import databases.org.opcfoundation.ua._2011._03.uanodeset.Reference;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UADataType;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAMethod;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANode;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAObject;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAObjectType;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAReferenceType;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAVariable;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAVariableType;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAView;
import externalFiles.ConsistencyCheck;
import externalFiles.LoadStore;
import logfilemanagement.LogFileManagement;
import servercommunication.ServerHandle;

public class OpcUaManagement {
	
	private static Logger logger = LogManager.getLogger(OpcUaManagement.class);
	
	private static UANodeSet serverInformationmodel = null;
	private static int failures = 0;
	private static HashMap<String, String> checkedNodes = new HashMap<String, String>();
	private static UANode currentXmlNode = null;
	
	private static void setXmlNode(UANode xmlNode) {
		currentXmlNode = xmlNode;
	}
	
	private static UANode getCurrentXmlNode() {
		return currentXmlNode;
	}
	
	public static void setFramework(String framework) {
		ServerHandle.setFramework(framework);
	}

	public static HashMap<String, String> getCheckNodes() {
		return checkedNodes;
	}

	public static int getFailures() {
		return failures;
	}

	public static boolean createClient(String serverUrl, String port, String securityPolicy) {

		if (ServerHandle.createClient(serverUrl, port, securityPolicy)) {
			logger.info(LogFileManagement.addErrorCode("Info003") + LogFileManagement.addErrorCode("Info023"));
			return true;
		} else {
			logger.error(LogFileManagement.addErrorCode("Error006") + serverUrl + ", " + port + ", " + securityPolicy);
			return false;
		}
	}

	public static boolean connect() {
		if (ServerHandle.connect()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean disconnect() {
		if (ServerHandle.disconnect()) {
			logger.info(LogFileManagement.addErrorCode("Info003") + LogFileManagement.addErrorCode("Info025"));
			return true;
		}
		return true;
	}

	public static UANodeSet loadXmlInformationModel(String xmlFile) {

		ConsistencyCheck.validateXmlAgainstXsd(xmlFile, "UANodeSet_opcf.xsd");

		// Implemented XML check using XSD
		// if(ConsistencyCheck.validateXmlAgainstXsd(xmlFile, "UANodeSet_opcf.xsd")) {
		
		UANodeSet xmlInformationModel = null;
		
		if (xmlFile != "") {
			xmlInformationModel = LoadStore.loadInformationModel(xmlFile);
		} else {
			logger.error(LogFileManagement.addErrorCode("Error004") + LogFileManagement.addErrorCode("Error005"));
		}
		
		return xmlInformationModel;
		
		// } else {
		// LogFileManagement.logMessage("error", "Error004",
		// LogFileManagement.addErrorCode("Error005"));
		// }

	}

	public static void createServerInformationmodel() {
		serverInformationmodel = ServerHandle.createServerInformationmodel();
	}

	public static UANodeSet getServerInformationmodel() {
		return serverInformationmodel;
	}

	public static List<UANode> findNode(String xmlNodeId, String xmlBrowseName, UANodeSet serverInformationmodel) {
		
		List<UANode> listOfNodes = new ArrayList<UANode>();
		
		List<UANode> serverNodes = serverInformationmodel.getUAObjectOrUAVariableOrUAMethod();
		
		for (int i = 0; i < serverNodes.size(); i++) {
			UANode serverNode = serverNodes.get(i);

			if(xmlNodeId.equals(serverNode.getNodeId())) {
				listOfNodes.add(serverNode);
				
				if(!checkedNodes.containsKey(xmlNodeId) && !checkedNodes.containsKey(xmlBrowseName)) {
					checkedNodes.put(xmlNodeId, xmlBrowseName);
				}
				
			} else if(xmlNodeId.equals("") && xmlBrowseName.equals(serverNode.getBrowseName())) {
				
				if(!checkedNodes.containsKey(xmlBrowseName)) {
					checkedNodes.put(xmlBrowseName, xmlNodeId);
				}
								
				listOfNodes.add(serverNode);
			}
			
		}
		
		if(listOfNodes.size() > 1) {
			logger.warn("There are several nodes with " + "nodeId = " + xmlNodeId + ", browseName = " + xmlBrowseName + ", amount = " + Integer.toString(listOfNodes.size()));
		}
		
		return listOfNodes;
	}

	public static UANode compareNode(UANode serverNode, UANode xmlNode) {
		
		// compare the node class of both objects and if unequal than compare just general attributes
		String result = null;
		
		setXmlNode(xmlNode);
		
		result = compareGeneralAttributes(serverNode, xmlNode);
		
		xmlNode = getCurrentXmlNode();
		
		if (xmlNode.getClass().getSimpleName().equals(serverNode.getClass().getSimpleName())) {
			switch (xmlNode.getClass().getSimpleName()) {
			
			case "UAObject":
				
				UAObject serverObjectNode = (UAObject) serverNode;
				UAObject xmlObjectNode = (UAObject) xmlNode;
				
				short eventNotifier = xmlObjectNode.getEventNotifier();
				if (eventNotifier != serverObjectNode.getEventNotifier()) {
					result += ", EventNotifier: " + serverObjectNode.getEventNotifier() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAVariable":
				
				UAVariable serverVariableNode = (UAVariable) serverNode;
				UAVariable xmlVariableNode = (UAVariable) xmlNode;
				
				if (xmlVariableNode.getValue() != null) {
					serverVariableNode.setValue(xmlVariableNode.getValue());
				}						
				
				if (xmlVariableNode.getDataType() != null) {
					String dataType = xmlVariableNode.getDataType();
					if (!dataType.equals(serverVariableNode.getDataType())) {
						result += ", DataType: " + serverVariableNode.getDataType() + LogFileManagement.addErrorCode("Bad0012");
					}
				} else {
					xmlVariableNode.setDataType(serverVariableNode.getDataType());
					result += ", DataType: " + serverVariableNode.getDataType() + LogFileManagement.addErrorCode("Bad0010");
				}
								
				int valueRank = xmlVariableNode.getValueRank();
				if (valueRank != serverVariableNode.getValueRank()) {
					result += ", ValueRank: " + serverVariableNode.getValueRank() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				if (xmlVariableNode.getArrayDimensions().size() > 0) {
					if (serverVariableNode.getArrayDimensions().size() > 0) {
						if(!xmlVariableNode.getArrayDimensions().containsAll(serverVariableNode.getArrayDimensions())) {
							result += ", ArrayDimension: " + serverVariableNode.getArrayDimensions() + LogFileManagement.addErrorCode("Bad0012");
						}
					} else {
						result += ", ArrayDimension: " + xmlVariableNode.getArrayDimensions() + LogFileManagement.addErrorCode("Bad0017");
					}
				} else {
					
					if (serverVariableNode.getArrayDimensions().size() != 0) {
						xmlVariableNode.getArrayDimensions().add(serverVariableNode.getArrayDimensions().get(0));
						result += ", ArrayDimension: " + serverVariableNode.getArrayDimensions().get(0) + LogFileManagement.addErrorCode("Bad0010");
					}
				}
				
				
				long accessLevel = xmlVariableNode.getAccessLevel();
				if (accessLevel != serverVariableNode.getAccessLevel()) {
					result += ", AccessLevel: " + serverVariableNode.getAccessLevel() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				long userAccessLevel = xmlVariableNode.getUserAccessLevel();
				if (userAccessLevel != serverVariableNode.getAccessLevel()) {
					result += ", UserAccessLevel: " + serverVariableNode.getAccessLevel() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				double minimumSamplingInterval = serverVariableNode.getMinimumSamplingInterval();
				if (minimumSamplingInterval != serverVariableNode.getMinimumSamplingInterval()) {
					result += ", MinimumSamplingInterval: " + serverVariableNode.getMinimumSamplingInterval() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				boolean historizing = xmlVariableNode.isHistorizing();
				if (historizing != serverVariableNode.isHistorizing()) {
					result += ", Historizing: " + serverVariableNode.isHistorizing() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAMethod":
				
				UAMethod serverMethodNode = (UAMethod) serverNode;
				UAMethod xmlMethodNode = (UAMethod) xmlNode;
				
				boolean executeable = xmlMethodNode.isExecutable();
				if (executeable != serverMethodNode.isExecutable()) {
					result += ", Executeable: " + LogFileManagement.addErrorCode("Bad0012");
				}
				
				boolean userExecuteable = xmlMethodNode.isExecutable();
				if (userExecuteable != serverMethodNode.isUserExecutable()) {
					result += ", UserExecuteable: " + serverMethodNode.isUserExecutable() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAObjectType":
				
				UAObjectType serverObjectTypeNode = (UAObjectType) serverNode;
				UAObjectType xmlObjectTypeNode = (UAObjectType) xmlNode;
				
				boolean isAbstract = xmlObjectTypeNode.isIsAbstract();
				if (isAbstract != serverObjectTypeNode.isIsAbstract()) {
					result += ", isAbstract: " + serverObjectTypeNode.isIsAbstract() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAVariableType":
				
				UAVariableType serverVariableTypeNode = (UAVariableType) serverNode;
				UAVariableType xmlVariableTypeNode = (UAVariableType) xmlNode;
				
				if (xmlVariableTypeNode.getValue() != null) {
					serverVariableTypeNode.setValue(xmlVariableTypeNode.getValue());
				}
				
				String dataType = xmlVariableTypeNode.getDataType();
				if (!dataType.equals(serverVariableTypeNode.getDataType())) {
					result += ", DataType: " + LogFileManagement.addErrorCode("Bad0012");
				}
				
				valueRank = xmlVariableTypeNode.getValueRank();
				if (valueRank != serverVariableTypeNode.getValueRank()) {
					result += ", ValueRank: " + serverVariableTypeNode.getValueRank() + LogFileManagement.addErrorCode("Bad0012");
				}
				
//				if(!xmlVariableTypeNode.getArrayDimensions().containsAll(serverVariableTypeNode.getArrayDimensions())) {
//					result += ", ArrayDimension: " + serverVariableTypeNode.getArrayDimensions() + LogFileManagement.addErrorCode("Bad0012");
//				}
				if (xmlVariableTypeNode.getArrayDimensions().size() > 0) {
					if (serverVariableTypeNode.getArrayDimensions().size() > 0) {
						if(!xmlVariableTypeNode.getArrayDimensions().containsAll(serverVariableTypeNode.getArrayDimensions())) {
							result += ", ArrayDimension: " + serverVariableTypeNode.getArrayDimensions() + LogFileManagement.addErrorCode("Bad0012");
						}
					} else {
						result += ", ArrayDimension: " + xmlVariableTypeNode.getArrayDimensions() + LogFileManagement.addErrorCode("Bad0017");
					}
				} else {
					
					if (serverVariableTypeNode.getArrayDimensions().size() != 0) {
						xmlVariableTypeNode.getArrayDimensions().add(serverVariableTypeNode.getArrayDimensions().get(0));
						result += ", ArrayDimension: " + serverVariableTypeNode.getArrayDimensions().get(0) + LogFileManagement.addErrorCode("Bad0010");
					}
				}
				
				isAbstract = xmlVariableTypeNode.isIsAbstract();
				if (isAbstract != serverVariableTypeNode.isIsAbstract()) {
					result += ", isAbstract: " + serverVariableTypeNode.isIsAbstract() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAReferenceType":
				
				UAReferenceType serverReferenceTypeNode = (UAReferenceType) serverNode;
				UAReferenceType xmlReferenceTypeNode = (UAReferenceType) xmlNode;
								
				if(xmlReferenceTypeNode.getInverseName().containsAll(serverReferenceTypeNode.getInverseName())) {
					result += ", InverseName: " + LogFileManagement.addErrorCode("Bad0012");
				}
				
				boolean symmetric = xmlReferenceTypeNode.isSymmetric();
				if (symmetric != serverReferenceTypeNode.isSymmetric()) {
					result += ", Symmetric: " + serverReferenceTypeNode.isSymmetric() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				isAbstract = xmlReferenceTypeNode.isIsAbstract();
				if (isAbstract != serverReferenceTypeNode.isIsAbstract()) {
					result += ", isAbstract: " + serverReferenceTypeNode.isIsAbstract() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UADataType":
				
				UADataType serverDataTypeNode = (UADataType) serverNode;
				UADataType xmlDataTypeNode = (UADataType) xmlNode;
				
				isAbstract = xmlDataTypeNode.isIsAbstract();
				if (isAbstract != serverDataTypeNode.isIsAbstract()) {
					result += ", isAbstract: " + serverDataTypeNode.isIsAbstract() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			case "UAView":
				
				UAView serverViewNode = (UAView) serverNode;
				UAView xmlViewNode = (UAView) xmlNode;
				
				boolean containsLoop = xmlViewNode.isContainsNoLoops();
				if (containsLoop != serverViewNode.isContainsNoLoops()) {
					result += ", ContainsNoLoop: " + serverViewNode.isContainsNoLoops() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				eventNotifier = xmlViewNode.getEventNotifier();
				if (eventNotifier != serverViewNode.getEventNotifier()) {
					result += ", EventNotifier: " + serverViewNode.getEventNotifier() + LogFileManagement.addErrorCode("Bad0012");
				}
				
				break;
				
			default:
				
				logger.error("NodeClass: " + xmlNode.getClass().getSimpleName() + " is unknown");
				
				break;
			}
		} else {
			
			result += ", NodeClass: " + LogFileManagement.addErrorCode("Bad0012");
			
		}	
		
		logger.warn(result);
		
		return xmlNode;
	}
	
	private static String compareGeneralAttributes(UANode serverNode, UANode xmlNode) {
		
		String result = "";
		
		if (xmlNode.getNodeId().equals("") || xmlNode.getNodeId() == null) {
			
			xmlNode.setNodeId(serverNode.getNodeId());
			result += ", NodeId: " + xmlNode.getNodeId() + LogFileManagement.addErrorCode("Bad0010");
			
		} else {
			result = "NodeId = " + xmlNode.getNodeId();
		}
		
		short accessRest = xmlNode.getAccessRestrictions();
		if (accessRest != serverNode.getAccessRestrictions()) {
			result += ", AccessRestrictions: " + serverNode.getAccessRestrictions() + LogFileManagement.addErrorCode("Bad0012");
		}
		
		if (xmlNode.getBrowseName() != null) {
			String browseName = xmlNode.getBrowseName();
			if (!browseName.equals(serverNode.getBrowseName())) {
				result += ", BrowseName: " + serverNode.getBrowseName() + LogFileManagement.addErrorCode("Bad0012");
			}
		} else {
			xmlNode.setBrowseName(serverNode.getBrowseName());
			result += ", BrowseName: " + serverNode.getBrowseName() + LogFileManagement.addErrorCode("Bad0010");
		}
		
		long userWriteMask = xmlNode.getUserWriteMask();
		if (userWriteMask != serverNode.getUserWriteMask()) {
			result += ", UserWriteMask: " + serverNode.getUserWriteMask() + LogFileManagement.addErrorCode("Bad0012");
		}
		
		long writeMask = xmlNode.getWriteMask();
		if (writeMask != serverNode.getWriteMask()) {
			result += ", WriteMask: " + serverNode.getWriteMask() + LogFileManagement.addErrorCode("Bad0012");
		}
		
		if (xmlNode.getDisplayName().size() > 0) {
			List<LocalizedText> listDisplayName = xmlNode.getDisplayName();
			for (int i = 0; i < listDisplayName.size(); i++) {
				
				LocalizedText displayName = listDisplayName.get(i);
				
				List<LocalizedText> serverListDisplayName = serverNode.getDisplayName();
				
				for (int j = 0; j < serverListDisplayName.size(); j++) {
					
					LocalizedText serverDisplayName  = serverListDisplayName.get(j);

					if (!(displayName.getValue().equals(serverDisplayName.getValue()) && displayName.getLocale().equals(serverDisplayName.getLocale()))) {
						result += ", Displayname: " + serverDisplayName.getValue() + LogFileManagement.addErrorCode("Bad0012");
					}
					
				}
				
			}
		} else {
			xmlNode.getDisplayName().add(serverNode.getDisplayName().get(0));
			result += ", Displayname: " + LogFileManagement.addErrorCode("Bad0010");
		}
		
		if (xmlNode.getDescription().size() > 0) {
			
			List<LocalizedText>  listDescription = xmlNode.getDescription();
			for (int i = 0; i < listDescription.size(); i++) {
				
				LocalizedText description = listDescription.get(i);
				
				List<LocalizedText> serverListDescription = serverNode.getDescription();
				
				for (int j = 0; j < serverListDescription.size(); j++) {
					
					LocalizedText serverDescription  = serverListDescription.get(j);

					if (!(description.getValue().equals(serverDescription.getValue()) && description.getLocale().equals(serverDescription.getLocale()))) {
						result += ", Description: " + serverDescription.getValue() + LogFileManagement.addErrorCode("Bad0012");
					}
					
				}
				
			}
			
		} else {
			if (serverNode.getDescription().size() > 0) {
				xmlNode.getDescription().add(serverNode.getDescription().get(0));
				result += ", Description: " + serverNode.getDescription() + LogFileManagement.addErrorCode("Bad0010");
			}
		}
		
		if (xmlNode.getReferences() != null && serverNode.getReferences() != null) {
			
			boolean differences = false;

			List<Reference> serverReferences = serverNode.getReferences().getReference();
			HashMap<String, Integer> foundReferences = new HashMap<String, Integer>();
			
			for (int j = 0; j < serverReferences.size(); j++) {

				Reference serverRef = serverReferences.get(j);
				
				for (int i = 0; i < xmlNode.getReferences().getReference().size(); i++) {
					
					Reference ref = xmlNode.getReferences().getReference().get(i);
					
					if (ref.getValue().startsWith(serverRef.getValue()) && ref.getReferenceType().equals(serverRef.getReferenceType())) {
						foundReferences.put(ref.getValue(), i);
					}
				}
			}
			
			for (int i = 0; i < serverReferences.size(); i++) {
				
				if (!foundReferences.containsKey(serverReferences.get(i).getValue())) {
					differences = true;
					
					Reference serverRef = serverReferences.get(i);
					serverRef.setValue(serverRef.getValue() + LogFileManagement.addErrorCode("Bad0010"));
					
					xmlNode.getReferences().getReference().add(serverRef);
				}
						
			}
			
			for (int i = 0; i < xmlNode.getReferences().getReference().size(); i++) {
				
				if (!(xmlNode.getReferences().getReference().get(i).getValue().contains("Bad") || foundReferences.containsKey(xmlNode.getReferences().getReference().get(i).getValue()))) {
					xmlNode.getReferences().getReference().get(i).setValue(xmlNode.getReferences().getReference().get(i).getValue() + LogFileManagement.addErrorCode("Bad0017"));
					differences = true;
				}
				
			}
			
			if (differences) {
				result += ", References: " + LogFileManagement.addErrorCode("Bad0012");
			}

		} else {
			xmlNode.setReferences(serverNode.getReferences());
			result += ", References: " + LogFileManagement.addErrorCode("Bad0010");
		}
		
		setXmlNode(xmlNode);
		
		return result;
		
	}

	public static void writeServerInformationModel(UANodeSet serverInformationModel, String fileName) {
		LoadStore.storeInformationModel(serverInformationModel, fileName);
	}
}
