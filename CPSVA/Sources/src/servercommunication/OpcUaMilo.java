/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is implementation of the Eclipse Milo framework and user defined methods.
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

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
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAVariable.Value;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAVariableType;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UAView;

public class OpcUaMilo {
	
	private static Logger logger = LogManager.getLogger(OpcUaMilo.class);
	
	private static boolean recursion = false;
	private static HashMap<String, String> stack = new HashMap<>();

	public static boolean createClient(String serverUrl, String port, String strSecurityPolicy) {

		if (OpcUaServicesMilo.createClient(serverUrl, port, strSecurityPolicy) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean connect() {
		return OpcUaServicesMilo.connect();
	}

	public static boolean disconnect() {
		return OpcUaServicesMilo.disconnect();
	}

	public static UANodeSet createServerInformationModel() {
		
		UANodeSet serverInformationModel = new UANodeSet();
		
		List<UANode> listOfNodes = new ArrayList<UANode>();
		
		NodeId nodeId = OpcUaServicesMilo.convertNodeId("0", "Numeric", "84");
		UANode node = defineNode(nodeId);
		
		if (node.getNodeId() != null && node.getBrowseName() != null) {
			listOfNodes.add(node);
			stack.put(node.getNodeId(), node.getBrowseName());
			
			listOfNodes = createRec(listOfNodes);
			
			serverInformationModel.getUAObjectOrUAVariableOrUAMethod().addAll(listOfNodes);
			
			return serverInformationModel;
		} else {
			logger.error("Error, while creating server informationmodel.");
			JOptionPane.showMessageDialog(null, "Error, while creating server informationmodel.");
			return null;
		}
		
	}
	
	public static List<UANode> createRec(List<UANode> listOfNodes) {
		
		List<Reference> referencesOfLastNode = listOfNodes.get(listOfNodes.size() - 1).getReferences().getReference();

		if (!referencesOfLastNode.isEmpty()) {
			for (int i = 0; i < referencesOfLastNode.size(); i++) {
				
				if (stack.get(referencesOfLastNode.get(i).getValue()) == null) {

					String ns = referencesOfLastNode.get(i).getValue().split(";")[0].split("=")[1];
					String identifierType = "";
					
					String identifier = referencesOfLastNode.get(i).getValue().split(";")[1].split("=")[1];
					
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
						identifier = referencesOfLastNode.get(i).getValue().split(";")[1].split("=")[2];
					break;
					default:
						logger.error("unkown identifier");
						break;
					}
					
					NodeId nodeId = OpcUaServicesMilo.convertNodeId(ns, identifierType, identifier);
					
					if (nodeId != null) {
						UANode node = defineNode(nodeId);

						listOfNodes.add(node);
						stack.put(node.getNodeId(), node.getBrowseName());

						listOfNodes = createRec(listOfNodes);
					}

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
		try {
			intNodeClass = (int) OpcUaServicesMilo.read(serverNodeId, 2).get().getResults()[0].getValue().getValue();
			
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		
		switch (intNodeClass) {
		case 1:
			
			UAObject objectNode = new UAObject();
			
			try {
				
				objectNode = (UAObject) setGeneralAttributesOfNode(objectNode, serverNodeId);
				
				// 12 represents the attribute eventNotifier according to the opc ua standard
				CompletableFuture<ReadResponse> response = OpcUaServicesMilo.read(serverNodeId, 12);
				
				UByte eventNotifer = (UByte) response.get().getResults()[0].getValue().getValue();
				
				objectNode.setEventNotifier(eventNotifer.shortValue());
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = objectNode;
			
			break;
			
		case 2:
			
			UAVariable variableNode = new UAVariable();
			
			try {
				
				variableNode = (UAVariable) setGeneralAttributesOfNode(variableNode, serverNodeId);
				
				CompletableFuture<ReadResponse> response = null;
				
				// 13 represents the attribute value according to the opc ua standard
				// response = OpcUaServicesMilo.read(serverNodeId, 13);
				variableNode.setValue(null);
				
				// 14 represents the attribute dataType according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 14);
				String dataType = response.get().getResults()[0].getValue().getValue().toString();
				dataType = dataType.substring(7, dataType.length()-1);
				variableNode.setDataType(dataType);
				
				// 15 represents the attribute valueRank according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 15);
				variableNode.setValueRank((Integer) response.get().getResults()[0].getValue().getValue());
				
				// 16 represents the attribute arrayDimension according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 16);
				UInteger arrayDimension[] = new UInteger[1];
				arrayDimension = (UInteger[]) response.get().getResults()[0].getValue().getValue();
				if(arrayDimension != null) {
					variableNode.getArrayDimensions().add(arrayDimension[0].toString());
				}
				
				// 17 represents the attribute accessLevel according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 17);
				variableNode.setAccessLevel(Long.valueOf((response.get().getResults()[0].getValue().getValue().toString())));
				
				// 18 represents the attribute userAccessLevel according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 18);
				variableNode.setUserAccessLevel(Long.valueOf((response.get().getResults()[0].getValue().getValue().toString())));
				
				// 19 represents the attribute minimumSamplingIntervall according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 19);
				variableNode.setMinimumSamplingInterval((double) response.get().getResults()[0].getValue().getValue());
				
				// 20 represents the attribute historizing according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 20);
				variableNode.setHistorizing((boolean) response.get().getResults()[0].getValue().getValue());
				
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = variableNode;
			
			break;
			
		case 4:
			
			UAMethod methodNode = new UAMethod();
			
			try {
				
				methodNode = (UAMethod) setGeneralAttributesOfNode(methodNode, serverNodeId);
				
				CompletableFuture<ReadResponse> response = null;
				
				// 21 represents the attribute executable according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 21);
				methodNode.setExecutable((boolean) response.get().getResults()[0].getValue().getValue());
				
				// 12 represents the attribute userExecutable according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 22);
				methodNode.setUserExecutable((boolean) response.get().getResults()[0].getValue().getValue());
				
			}  catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = methodNode;
			
			break;
			
		case 8:
			
			UAObjectType objectTypeNode = new UAObjectType();
			
			try {
				
				objectTypeNode = (UAObjectType) setGeneralAttributesOfNode(objectTypeNode, serverNodeId);
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				 CompletableFuture<ReadResponse> response = OpcUaServicesMilo.read(serverNodeId, 8);
				 objectTypeNode.setIsAbstract((boolean) response.get().getResults()[0].getValue().getValue());
				
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = objectTypeNode;
			
			break;
			
		case 16:
			
			UAVariableType variableTypeNode = new UAVariableType();
			
			try {
				
				variableTypeNode = (UAVariableType) setGeneralAttributesOfNode(variableTypeNode, serverNodeId);
				
				CompletableFuture<ReadResponse> response = null;
				
				// 13 represents the attribute dataType according to the opc ua standard
				// response = OpcUaServicesMilo.read(serverNodeId, 13);
				variableTypeNode.setValue(null);
				
				// 14 represents the attribute dataType according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 14);
				String dataType = response.get().getResults()[0].getValue().getValue().toString();
				dataType = dataType.substring(7, dataType.length()-1);
				variableTypeNode.setDataType(dataType);
				
				// 15 represents the attribute valueRank according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 15);
				variableTypeNode.setValueRank((Integer) response.get().getResults()[0].getValue().getValue());
				
				// 16 represents the attribute arrayDimensions according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 16);
				UInteger arrayDimension[] = new UInteger[1];
				arrayDimension = (UInteger[]) response.get().getResults()[0].getValue().getValue();
				if(arrayDimension != null) {
					variableTypeNode.getArrayDimensions().add(arrayDimension[0].toString());
				}
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 8);
				variableTypeNode.setIsAbstract((boolean) response.get().getResults()[0].getValue().getValue());
				
			}  catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = variableTypeNode;
			
			break;
			
		case 32:
			
			UAReferenceType referenceTypeNode = new UAReferenceType();
			
			try {
				
				referenceTypeNode = (UAReferenceType) setGeneralAttributesOfNode(referenceTypeNode, serverNodeId);
				
				CompletableFuture<ReadResponse> response = null;
				
				// 10 represents the attribute inverseName according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 10);
				LocalizedText text = new LocalizedText();
				
				Object objInverName = response.get().getResults()[0].getValue().getValue();
				
				text = castLocalizedText(objInverName);
				
				if(text.getValue() != null || !text.getLocale().equals("")) {
					referenceTypeNode.getInverseName().add(text);
				}
				
				
				// 9 represents the attribute symmetric according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 9);
				referenceTypeNode.setSymmetric((boolean) response.get().getResults()[0].getValue().getValue());
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 8);
				referenceTypeNode.setIsAbstract((boolean) response.get().getResults()[0].getValue().getValue());
				
			}  catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			node = referenceTypeNode;
			
			break;
			
		case 64:
			
			UADataType dataTypeNode = new UADataType();
			
			try {
				
				dataTypeNode = (UADataType) setGeneralAttributesOfNode(dataTypeNode, serverNodeId);
				
				// 8 represents the attribute isAbstract according to the opc ua standard
				 CompletableFuture<ReadResponse> response = OpcUaServicesMilo.read(serverNodeId, 8);
				 dataTypeNode.setIsAbstract((boolean) response.get().getResults()[0].getValue().getValue());
				
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}

			
			node = dataTypeNode;
			
			break;
			
		case 128:
			
			UAView viewNode = new UAView();
			
			try {
				
				viewNode = (UAView) setGeneralAttributesOfNode(viewNode, serverNodeId);
				
				CompletableFuture<ReadResponse> response = null;
				
				// 11 represents the attribute containsNoLoops according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 11);
				viewNode.setContainsNoLoops((boolean) response.get().getResults()[0].getValue().getValue());
				
				// 12 represents the attribute eventNotifier according to the opc ua standard
				response = OpcUaServicesMilo.read(serverNodeId, 12);
				
				UByte value = (UByte) response.get().getResults()[0].getValue().getValue();
				
				viewNode.setEventNotifier(value.shortValue()); 
				
			} catch (InterruptedException | ExecutionException e) {
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
		
		try {
			QualifiedName browseName = (QualifiedName) OpcUaServicesMilo.read(serverNodeId, 3).get().getResults()[0].getValue().getValue();
			node.setBrowseName(browseName.getNamespaceIndex() + ":" + browseName.getName());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		
		try {
			node.setNodeId(nodeIdToString((NodeId) OpcUaServicesMilo.read(serverNodeId, 1).get().getResults()[0].getValue().getValue()));
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		
		try {			
			node.setUserWriteMask(Long.valueOf(OpcUaServicesMilo.read(serverNodeId, 7).get().getResults()[0].getValue().getValue().toString()));
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		
		try {
			node.setWriteMask(Long.valueOf(OpcUaServicesMilo.read(serverNodeId, 6).get().getResults()[0].getValue().getValue().toString()));
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		
		LocalizedText locDisplayName = new LocalizedText();
		try {
			Object objDisplayName = OpcUaServicesMilo.read(serverNodeId, 4).get().getResults()[0].getValue().getValue();
			
			locDisplayName = castLocalizedText(objDisplayName);
			
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		if(locDisplayName.getValue() != null || !locDisplayName.getLocale().equals("")) {
			node.getDisplayName().add(locDisplayName);
		}
		
		LocalizedText locDescription = new LocalizedText();
		try {
			Object objDescription = OpcUaServicesMilo.read(serverNodeId, 5).get().getResults()[0].getValue().getValue();
			
			locDescription = castLocalizedText(objDescription);
			
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		if(locDescription.getValue() != null || !locDescription.getLocale().equals("")) {
			node.getDescription().add(locDescription);
		}
		
		ReferenceDescription[] refDes = OpcUaServicesMilo.getReferencesOfNode(serverNodeId);
		
		ListOfReferences references = new ListOfReferences();
		
		for (int i = 0; i < refDes.length; i++) {
			
			Reference reference = new Reference();
			
			reference.setIsForward(refDes[i].getIsForward());
			// Get part of the browseName. This part represents the type of a reference
			CompletableFuture<QualifiedName> refQualiName = OpcUaServicesMilo.getBrowseNameOfNode(refDes[i].getReferenceTypeId());
			String referenceType = "";
			try {
				referenceType = refQualiName.get().getName().toString();
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			
			reference.setReferenceType(referenceType);
			reference.setValue(formatNodeId(refDes[i].getNodeId().getNamespaceIndex().intValue(), refDes[i].getNodeId().getType().name(), refDes[i].getNodeId().getIdentifier().toString())); // adjust to get the right format of the nodeid
			
			references.getReference().add(reference);
		}
		node.setReferences(references);
		
		return node;
	}
	
	private static String nodeIdToString(NodeId id) {
		
		String strNodeId = null;
		
		switch (id.getType()) {
		case Numeric:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";i=" + id.getIdentifier();
			break;
		case String:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";s=" + id.getIdentifier();
			break;
		case Guid:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";g=" + id.getIdentifier();
			break;
		case Opaque:
			strNodeId = "ns=" + id.getNamespaceIndex() + ";b=" + id.getIdentifier();
			break;

		default:
			break;
		}
		
		return strNodeId;
	}

	private static LocalizedText castLocalizedText(Object object) {
		
		LocalizedText text = new LocalizedText();
		
		if (object != null) {
			Method methods[] = object.getClass().getMethods();
			
			for (int i = 0; i < methods.length; i++) {
				
				if (methods[i].getName().equals("getLocale")) {
					Method getLocale = methods[i];
					try {
						String locale = (String) getLocale.invoke(object, null);
						if(locale != null) {
							text.setLocale(locale);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.error(e.getMessage());
					}
				}
				if (methods[i].getName().equals("getText")) {
					Method getText = methods[i];
					try {
						String value = (String) getText.invoke(object, null);
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
