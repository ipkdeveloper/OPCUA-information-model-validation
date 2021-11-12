/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is implementation opc ua services of the OPF Foundation Java Legacy framework.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */


package servercommunication;

import static org.opcfoundation.ua.utils.EndpointUtil.selectByMessageSecurityMode;
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;
import static org.opcfoundation.ua.utils.EndpointUtil.sortBySecurityLevel;

import java.math.BigInteger;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ByteString;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResult;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReadRequest;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteRequest;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.core.WriteValue;

public class OpcUaServicesOPCF {

	private static Logger logger = LogManager.getLogger(OpcUaServicesOPCF.class);
	
	private static Client client = null;
	private static SessionChannel mySession = null;
	private static boolean active = false;

	public static Client getClient() {
		return client;
	}

	public static void setClient(Client client) {
		OpcUaServicesOPCF.client = client;
	}

	// Activate the session
	public static boolean activateSession() {
		if (mySession == null) {
			logger.warn("mySession=null"  + ", create opc ua session before trying to connect");
			return false;
		} else {
			try {
				mySession.activate();
				active = true;
				return true;
			} catch (ServiceResultException e) {
				logger.error(e.getMessage());

			}
		}
		return false;
	}

	// Check whether session is active or not
	public boolean isActive() {
		return active;
	}

	// User defined method
	// Create Session and Client and activate the Session
	public static void createSession(String serverUrl, String port, String strSecurityPolicy) {
		
		setClient(Client.createClientApplication(null));

		serverUrl = "opc.tcp://" + serverUrl + ":" + port;

		try {
			EndpointDescription[] endpoints = client.discoverEndpoints(serverUrl);

			if (serverUrl.startsWith("opc.tcp")) {
				endpoints = selectByProtocol(endpoints, "opc.tcp");

				// Filter out all but Signed & Encrypted endpoints
				endpoints = selectByMessageSecurityMode(endpoints, MessageSecurityMode.None);

				// Filter out all but Basic128 cryption endpoints
				// endpoints = selectBySecurityPolicy(endpoints, SecurityPolicy.BASIC128RSA15);

				// Sort endpoints by security level. The lowest level at the
				// beginning, the highest at the end of the array
				// here, choose the highest security level
				endpoints = sortBySecurityLevel(endpoints);

			} else {
				endpoints = selectByProtocol(endpoints, "opc.https");
			}

			// Choose one endpoint
			// here: EndpointDescription endpoint = endpoints[endpoints.length - 1]; -> the
			// highest security level
			EndpointDescription endpoint = endpoints[0];

			mySession = client.createSessionChannel(endpoint);
// activate in methode connect()
			activateSession();
//			mySession.activate();

//			active = true;

		} catch (ServiceResultException e) {
			logger.error(e.getMessage());
		}
	}

	// Disconnect the client from server
	public static boolean closeSession() {
		if (active) {
			mySession.closeAsync();
			return true;
		}
		return false;
	}
	
	// browse service
	public static ReferenceDescription[] browse(NodeId nodeId) {
		
		if(nodeId != null) {			
			BrowseDescription browse = new BrowseDescription();
			browse.setNodeId(nodeId);
			browse.setBrowseDirection(BrowseDirection.Both);
			browse.setIncludeSubtypes(true);
			browse.setNodeClassMask(
					NodeClass.Object, NodeClass.Variable,
					NodeClass.Method, NodeClass.DataType,
					NodeClass.ReferenceType, NodeClass.ObjectType,
					NodeClass.VariableType, NodeClass.Unspecified, NodeClass.View
					);
			browse.setResultMask(new UnsignedInteger(BrowseResultMask.All.getValue()));


			try {
				BrowseResponse res = mySession.Browse(null, null, null, browse);
				BrowseResult[] result = res.getResults();

				return result[0].getReferences();

			} catch (ServiceResultException e) {
				logger.error(e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	// read
	public static ReadResponse read(NodeId nodeId, UnsignedInteger attribute) {
		
		if(nodeId != null) {
			ReadValueId[] nodesToRead = { new ReadValueId(nodeId, attribute, null, null) };
			ReadRequest req = new ReadRequest(null, 0.0, TimestampsToReturn.Both, nodesToRead);

			ReadResponse res = null;
			try {
				res = mySession.Read(req);
				return res;
			} catch (ServiceResultException e) {
				logger.error(e.getMessage());
			}
		}
		return null;
	}

	// write
	public static String write(String ns, String identifiertype, String identifier, DataValue value) {

		NodeId nodeId = convertNodeId(ns, identifiertype, identifier);
		
		if(nodeId != null) {
			WriteValue[] writeValue = { new WriteValue(nodeId, Attributes.BrowseName, null, value) };
			WriteRequest req = new WriteRequest(null, writeValue);

			WriteResponse res = null;

			try {
				res = mySession.Write(req);			
			} catch (ServiceResultException e) {
				e.printStackTrace();
			}

			return res.getResults()[0].getValue().toString();
		}
		return null;
	}
	
	public static Object getQualifiedNameOfNode(NodeId nodeId) {
		
		if(nodeId != null) {
			ReadValueId[] nodesToRead = { new ReadValueId(nodeId, Attributes.BrowseName, null, null) };
			ReadRequest req = new ReadRequest(null, 0.0, TimestampsToReturn.Both, nodesToRead);

			ReadResponse res = null;
			try {
				res = mySession.Read(req);
			} catch (ServiceResultException e) {
				logger.error(e.getMessage());
			}

			return res.getResults()[0].getValue().getValue();
		}
		return "";
	}

	// User defined method to convert a nodeid as a string to a Milo nodeid
	static NodeId convertNodeId(String ns, String identifierType, String identifier) {
		NodeId nodeid = null;
		if(!ns.equals("") && !identifierType.equals("") && !identifier.equals("")) {
			int namespaceIndex = Integer.parseInt(ns);
			
			switch (identifierType) {
			case "Numeric": 
				nodeid = new NodeId(namespaceIndex, UnsignedInteger.parseUnsignedInteger(identifier));
				break;
			case "String":
				nodeid = new NodeId(namespaceIndex, identifier);
				break;
			case "Guid":
				nodeid = new NodeId(namespaceIndex, UUID.fromString(identifier));
				break;
			case "Opaque":
				
				String numbers = identifier.split("x")[1];
				
				byte[] byteArray = new byte[numbers.length() / 2];
				
				int i = 0;
				int byteArrayIndex = 0;
				while (i <= numbers.length() - 2) {
					
					String numberAsHex = numbers.substring(i, i + 2);
					
					Integer numberAsDec = new Integer(Integer.parseInt(numberAsHex, 16));
					
					
					byteArray[byteArrayIndex] = numberAsDec.byteValue();
					
					byteArrayIndex = byteArrayIndex + 1;
					i = i + 2;
				}
				
				ByteString byteString = null;
				byteString = ByteString.valueOf(byteArray);
				
				nodeid = new NodeId(namespaceIndex, byteString);
				break;
			default:
				System.out.println("Error by creating nodeid");
				break;
			}
		}
		return nodeid;
	}
	
}
