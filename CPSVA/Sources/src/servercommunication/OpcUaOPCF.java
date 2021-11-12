/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is implementation of the OPC Foundation Java Legacy
 * 				framework and user defined methods.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */


package servercommunication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReferenceDescription;

import databases.org.opcfoundation.ua._2011._03.uanodeset.ListOfReferences;
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

public class OpcUaOPCF {
	
	private static Logger logger = LogManager.getLogger(OpcUaOPCF.class);

	private static boolean recursion = false;
	private static HashMap<String, String> stack = new HashMap<>();

 	public static boolean createSession(String serverUrl, String port, String strSecurityPolicy) {

		try {
			OpcUaServicesOPCF.createSession(serverUrl, port, strSecurityPolicy);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;

		}
	}

	public static boolean activateSession() {
		return OpcUaServicesOPCF.activateSession();
	}

	public static boolean closeSession() {
		return OpcUaServicesOPCF.closeSession();
	}

	public static UANodeSet createServerInformationmodel() {
		
		UANodeSet serverInformationModel = new UANodeSet();
		
		List<UANode> listOfNodes = new ArrayList<UANode>();
		
		NodeId nodeId = OpcUaServicesOPCF.convertNodeId("0", "Numeric", "84");
		UANode node = defineNode(nodeId);
		
		listOfNodes.add(node);
		stack.put(node.getNodeId(), node.getBrowseName());
		
		listOfNodes = createRec(listOfNodes);
		serverInformationModel.getUAObjectOrUAVariableOrUAMethod().addAll(listOfNodes);
		
		return serverInformationModel;
	}

	public static List<UANode> createRec(List<UANode> listOfNodes) {
		
		List<Reference> referencesOfLastNode = listOfNodes.get(listOfNodes.size() - 1).getReferences().getReference();
		
		if (!referencesOfLastNode.isEmpty()) {
			for (int i = 0; i < referencesOfLastNode.size(); i++) {

				if (stack.get(referencesOfLastNode.get(i).getValue()) == null) {

					String ns = referencesOfLastNode.get(i).getValue().split(";")[0].split("=")[1];
					String identifierType = "";

					switch (referencesOfLastNode.get(i).getValue().split(";")[1].split("=")[0]) {
					case "i":
						identifierType = "Numeric";
						break;
					case "s":
						identifierType = "String";
						break;
					case "g":
						identifierType = "Guid";
						break;
					case "b":
						identifierType = "Opaque";
					break;

					default:
						logger.error("unkown identifier");
						break;
					}
					
					String identifier = referencesOfLastNode.get(i).getValue().split(";")[1].split("=")[1];
					NodeId nodeId = OpcUaServicesOPCF.convertNodeId(ns, identifierType, identifier);

					// UaNode serverNode = OpcUaServicesOPCF.getNodeOfNodeId(ns, identifierType, identifier);

					UANode node = defineNode(nodeId);

					listOfNodes.add(node);
					stack.put(node.getNodeId(), node.getBrowseName());

					listOfNodes = createRec(listOfNodes);

				} else {
					if(!recursion) {
						recursion = true;
						logger.warn("recursion detected");
					}
				}
			} 
		}
		return listOfNodes;
	}
	
	public static UANode defineNode(NodeId serverNodeId) {
		
		UANode node = new UANode();
		
		int intNodeClass = 0;
		intNodeClass = (int) OpcUaServicesOPCF.read(serverNodeId, Attributes.NodeClass).getResults()[0].getValue().getValue();
		
		switch (intNodeClass) {
		case 1:
			
			UAObject objectNode = new UAObject();
			
			try {
				
				objectNode = (UAObject) setGeneralAttributesOfNode(objectNode, serverNodeId);
				
				// 12 represents the attribute eventNotifier according to the opc ua standard
				ReadResponse response = OpcUaServicesOPCF.read(serverNodeId, Attributes.EventNotifier);
								
				objectNode.setEventNotifier(Short.valueOf(response.getResults()[0].getValue().getValue().toString()));
			} catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = objectNode;
			
			break;
			
		case 2:
			
			UAVariable variableNode = new UAVariable();
			
			try {
				
				variableNode = (UAVariable) setGeneralAttributesOfNode(variableNode, serverNodeId);
				
				ReadResponse response = null;
				
				// 13 represents the attribute value according to the opc ua standard
				// response = OpcUaServicesOPCF.read(serverNodeId, Attributes.Value);
				variableNode.setValue(null);
				
				// 14 represents the attribute dataType according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.DataType);
				variableNode.setDataType(response.getResults()[0].getValue().getValue().toString());
				
				// 15 represents the attribute valueRank according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.ValueRank);
				variableNode.setValueRank((Integer) response.getResults()[0].getValue().getValue());
				
				// 16 represents the attribute arrayDimension according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.ArrayDimensions);
				if(response.getResults()[0].getValue().getValue() != null) {
					UnsignedInteger arrayDimension[] = (UnsignedInteger[]) response.getResults()[0].getValue().getValue();
					variableNode.getArrayDimensions().add(arrayDimension[0].toString());
				}
				
				// 17 represents the attribute accessLevel according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.AccessLevel);
				variableNode.setAccessLevel(Long.valueOf((response.getResults()[0].getValue().getValue().toString())));
				
				// 18 represents the attribute userAccessLevel according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.UserAccessLevel);
				variableNode.setUserAccessLevel(Long.valueOf((response.getResults()[0].getValue().getValue().toString())));
				
				// 19 represents the attribute minimumSamplingIntervall according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.MinimumSamplingInterval);
				variableNode.setMinimumSamplingInterval((double) response.getResults()[0].getValue().getValue());
				
				// 20 represents the attribute historizing according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.Historizing);
				variableNode.setHistorizing((boolean) response.getResults()[0].getValue().getValue());
				
			} catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = variableNode;
			
			break;
			
		case 4:
			
			UAMethod methodNode = new UAMethod();
			
			try {
				
				methodNode = (UAMethod) setGeneralAttributesOfNode(methodNode, serverNodeId);
				
				ReadResponse response = null;
				
				// 21 represents the attribute executable according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.Executable);
				methodNode.setExecutable((boolean) response.getResults()[0].getValue().getValue());
				
				// 12 represents the attribute userExecutable according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.UserExecutable);
				methodNode.setUserExecutable((boolean) response.getResults()[0].getValue().getValue());
				
			}  catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = methodNode;
			
			break;
			
		case 8:
			
			UAObjectType objectTypeNode = new UAObjectType();
			
			try {
				
				objectTypeNode = (UAObjectType) setGeneralAttributesOfNode(objectTypeNode, serverNodeId);
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				 ReadResponse response = OpcUaServicesOPCF.read(serverNodeId, Attributes.IsAbstract);
				 objectTypeNode.setIsAbstract((boolean) response.getResults()[0].getValue().getValue());
				
			} catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = objectTypeNode;
			
			break;
			
		case 16:
			
			UAVariableType variableTypeNode = new UAVariableType();
			
			try {
				
				variableTypeNode = (UAVariableType) setGeneralAttributesOfNode(variableTypeNode, serverNodeId);
				
				ReadResponse response = null;
				
				// 13 represents the attribute value according to the opc ua standard
				// response = OpcUaServicesOPCF.read(serverNodeId, Attributes.Value);
				variableTypeNode.setValue(null);
				
				// 14 represents the attribute dataType according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.DataType);
				variableTypeNode.setDataType(response.getResults()[0].getValue().getValue().toString());
				
				// 15 represents the attribute valueRank according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.ValueRank);
				variableTypeNode.setValueRank((Integer) response.getResults()[0].getValue().getValue());
				
				// 16 represents the attribute arrayDimensions according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.ArrayDimensions);
				if(response.getResults()[0].getValue().getValue() != null) {
					UnsignedInteger arrayDimension[] = (UnsignedInteger[]) response.getResults()[0].getValue().getValue();
					variableTypeNode.getArrayDimensions().add(arrayDimension[0].toString());
				}
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.IsAbstract);
				variableTypeNode.setIsAbstract((boolean) response.getResults()[0].getValue().getValue());
				
			}  catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = variableTypeNode;
			
			break;
			
		case 32:
			
			UAReferenceType referenceTypeNode = new UAReferenceType();
			
			try {
				
				referenceTypeNode = (UAReferenceType) setGeneralAttributesOfNode(referenceTypeNode, serverNodeId);
				
				ReadResponse response = null;
				
				// 10 represents the attribute inverseName according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.InverseName);
				LocalizedText text = new LocalizedText();
				
				Object objInverName = response.getResults()[0].getValue().getValue();
				
				text = castLocalizedText(objInverName);
				
				if(text.getValue() != null || !text.getLocale().equals("")) {
					referenceTypeNode.getInverseName().add(text);
				}
				
				
				// 9 represents the attribute symmetric according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.Symmetric);
				referenceTypeNode.setSymmetric((boolean) response.getResults()[0].getValue().getValue());
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.IsAbstract);
				referenceTypeNode.setIsAbstract((boolean) response.getResults()[0].getValue().getValue());
				
			}  catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = referenceTypeNode;
			
			break;
			
		case 64:
			
			UADataType dataTypeNode = new UADataType();
						
			try {
				
				dataTypeNode = (UADataType) setGeneralAttributesOfNode(dataTypeNode, serverNodeId);
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				 ReadResponse response = OpcUaServicesOPCF.read(serverNodeId, Attributes.IsAbstract);
				 dataTypeNode.setIsAbstract((boolean) response.getResults()[0].getValue().getValue());
				
			} catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = dataTypeNode;
			
			break;
			
		case 128:
			
			UAView viewNode = new UAView();
			
			try {
				
				viewNode = (UAView) setGeneralAttributesOfNode(viewNode, serverNodeId);
				
				ReadResponse response = null;
				
				// 11 represents the attribute containsNoLoops according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.ContainsNoLoops);
				viewNode.setContainsNoLoops((boolean) response.getResults()[0].getValue().getValue());
				
				// 12 represents the attribute eventNotifier according to the opc ua standard
				response = OpcUaServicesOPCF.read(serverNodeId, Attributes.EventNotifier);
				
				UnsignedByte value = (UnsignedByte) response.getResults()[0].getValue().getValue();
				
				viewNode.setEventNotifier(value.shortValue());
				
				
			} catch (ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = viewNode;
			
			break;
			
		default:			
			logger.error("unkown nodeclass= " + intNodeClass);
			break;
		}
		
		return node;
	}
	
	public static UANode setGeneralAttributesOfNode(UANode node, NodeId serverNodeId) throws ExecutionException {
		
		QualifiedName browseName = (QualifiedName) OpcUaServicesOPCF.read(serverNodeId, Attributes.BrowseName).getResults()[0].getValue().getValue();
		
		node.setBrowseName(browseName.getNamespaceIndex() + ":" + browseName.getName());
		
		node.setNodeId(nodeIdToString((NodeId) OpcUaServicesOPCF.read(serverNodeId, Attributes.NodeId).getResults()[0].getValue().getValue()));
		
		node.setUserWriteMask(Long.valueOf(OpcUaServicesOPCF.read(serverNodeId, Attributes.UserWriteMask).getResults()[0].getValue().getValue().toString()));
		
		node.setWriteMask(Long.valueOf(OpcUaServicesOPCF.read(serverNodeId, Attributes.WriteMask).getResults()[0].getValue().getValue().toString()));
		
		LocalizedText locDisplayName = new LocalizedText();
		Object objDisplayName = OpcUaServicesOPCF.read(serverNodeId, Attributes.DisplayName).getResults()[0].getValue().getValue();
		
		locDisplayName = castLocalizedText(objDisplayName);
		if(locDisplayName.getValue() != null || !locDisplayName.getLocale().equals("")) {
			node.getDisplayName().add(locDisplayName);
		}
		
		LocalizedText locDescription = new LocalizedText();
		Object objDescription = OpcUaServicesOPCF.read(serverNodeId, Attributes.Description).getResults()[0].getValue().getValue();
		
		locDescription = castLocalizedText(objDescription);
		if(locDescription.getValue() != null || !locDescription.getLocale().equals("")) {
			node.getDescription().add(locDescription);
		}
		
		ReferenceDescription[] refDes = OpcUaServicesOPCF.browse(serverNodeId);
		
		ListOfReferences references = new ListOfReferences();
		
		for (int i = 0; i < refDes.length; i++) {
			
			Reference reference = new Reference();
			
			reference.setIsForward(refDes[i].getIsForward());
			// Get part of the browseName. This part represents the type of a reference
			QualifiedName refQualiName = (QualifiedName) OpcUaServicesOPCF.getQualifiedNameOfNode(refDes[i].getReferenceTypeId());
			String referenceType = "";
			referenceType = refQualiName.getName().toString();
			
			reference.setReferenceType(referenceType);
			reference.setValue(formatNodeId(refDes[i].getNodeId().getNamespaceIndex(), refDes[i].getNodeId().getIdType().name(), refDes[i].getNodeId().getValue().toString()));
			
			references.getReference().add(reference);
		}
		node.setReferences(references);
		
		return node;
	}
	
	private static LocalizedText castLocalizedText(Object object) {
		
		LocalizedText text = new LocalizedText();
		
		if (object != null) {
		
			Method methods[] = object.getClass().getMethods();
			
			Locale locale = null;
			String value = null;
			
			for (int i = 0; i < methods.length; i++) {
				
				if (methods[i].getName().equals("getLocale")) {
					Method getLocale = methods[i];
					try {
						locale = (Locale) getLocale.invoke(object, null);
						if(locale != null) {
							text.setLocale(locale.getLanguage());
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.error(e.getMessage());
					}
				}
				if (methods[i].getName().equals("getText")) {
					Method getText = methods[i];
					try {
						value = (String) getText.invoke(object, null);
						if(value != null) {
							text.setValue(value);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.error(e.getMessage());
					}
				}			
			}
		
		}
		
		return text;
	}
	
	private static String nodeIdToString(NodeId id) {
		
		String strNodeId = null;
		
		switch (id.getIdType()) {
		case Numeric:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";i=" + id.getValue().toString();
			break;
		case String:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";s=" + id.getValue().toString();
			break;
		case Guid:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";g=" + id.getValue().toString();
			break;
		case Opaque:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";b=" + id.getValue().toString();
			break;

		default:
			break;
		}
		
		return strNodeId;
	}
	
	public static String formatNodeId(int ns, String identifierType, String identifier) {
		String nodeId = null;
		
		switch (identifierType) {
		case "Numeric":
			nodeId = "ns=" + ns + ";i=" + identifier;
			break;
		case "String":
			nodeId = "ns=" + ns + ";s=" + identifier;
			break;
		case "Guid":
			nodeId = "ns=" + ns + ";g=" + identifier;
			break;
		case "Opaque":
			nodeId = "ns=" + ns + ";b=" + identifier;
			break;
		default:
			logger.error("unkown identifiertype= " + identifierType);
			break;
		}

		return nodeId;
	}

}
