/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is implementation opc ua services of the Eclipse Milo framework.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */


package servercommunication;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ApplicationDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowsePath;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowsePathResult;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.RelativePath;
import org.eclipse.milo.opcua.stack.core.types.structured.RelativePathElement;
import org.eclipse.milo.opcua.stack.core.types.structured.TranslateBrowsePathsToNodeIdsResponse;
import org.apache.logging.log4j.Logger;

public class OpcUaServicesMilo {
	
	private static OpcUaClient client = null;
	private static boolean connected = false;
	private final AtomicLong clientHandles = new AtomicLong(1L);
	private static Logger logger = LogManager.getLogger(OpcUaServicesMilo.class);
	private static SecurityPolicy securityPolicy = SecurityPolicy.None;

	// FindServers Service with a boolean return variable
	public ApplicationDescription[] findServers(String serverUrl, String port) {
		serverUrl = serverUrl + Integer.parseInt(port);
		try {
			ApplicationDescription[] discoveryEndpoints = UaTcpStackClient.findServers(serverUrl).get();
			return discoveryEndpoints;
		} catch (Exception e) {
			logger.error(e.getMessage());

		}
		return null;
	}

	// GetEndpoints Service with a boolean return variable
	public EndpointDescription getEndpoints(String serverUrl, String port) {
		serverUrl = serverUrl + Integer.parseInt(port);
		try {
			SecurityPolicy securityPolicy = SecurityPolicy.None;
			EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(serverUrl).get();
			EndpointDescription endpoint = Arrays.stream(endpoints)
					.filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri())).findFirst()
					.orElseThrow(() -> new Exception("no desired endpoints returned"));
			return endpoint;
		} catch (Exception e) {
			logger.error(e.getMessage());

		}
		return null;
	}
	
	// User defined method
	// GetEndpoints and use them for creating a client
	public static OpcUaClient createClient(String serverUrl, String port, String strSecurityPolicy) {

		switch (strSecurityPolicy) {
		case "none":
			securityPolicy = SecurityPolicy.None;
			break;
		case "basic128rsa15":
			securityPolicy = SecurityPolicy.Basic128Rsa15;
			break;
		case "basic256":
			securityPolicy = SecurityPolicy.Basic256;
			break;
		case "basic256sha256":
			securityPolicy = SecurityPolicy.Basic256Sha256;
			break;
		default:
			securityPolicy = SecurityPolicy.None;
			logger.error("SecurityPolicy= " + strSecurityPolicy);
			logger.warn("securityPolicy=none as default");
			break;
		}

		serverUrl = "opc.tcp://" + serverUrl + ":" + Integer.parseInt(port);
		try {
			EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(serverUrl).get();
			EndpointDescription endpoint = Arrays.stream(endpoints)
					.filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri())).findFirst()
					.orElseThrow(() -> new Exception("no desired endpoints returned"));

			// GESI workaround
			if (serverUrl.startsWith("opc.tcp://141.58.122.40")) {
				String temp = "141.58.122.40";
				endpoint = updateEndpointUrl(endpoint, temp);
			}

			// HMI Workaround
			// endpoint.setEndpointUrl(serverUrl); 

			OpcUaClientConfig config = OpcUaClientConfig.builder()
					.setApplicationName(LocalizedText.english("OPCUA Service Adapter Client"))
					.setApplicationUri("urn:/FraunhoferIPK/UM/OPCUAServiceAdapterClient").setEndpoint(endpoint)
					.setIdentityProvider(new AnonymousProvider()).setRequestTimeout(uint(5000)).build();
			client = new OpcUaClient(config);
			return client;

		} catch (Exception e) {
			logger.error(e.getMessage());
			client = null;
		}
		return client;
	}

	// Update the endpoints from the Server
	// Some servers give endpoints in a wrong fromat
	private static EndpointDescription updateEndpointUrl(EndpointDescription original, String hostname) {

		URI uri = null;
		try {
			uri = new URI(original.getEndpointUrl()).parseServerAuthority();
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
		}

		String endpointUrl = String.format("%s://%s:%s%s", uri.getScheme(), hostname, uri.getPort(), uri.getPath());
		return new EndpointDescription(endpointUrl, original.getServer(), original.getServerCertificate(),
				original.getSecurityMode(), original.getSecurityPolicyUri(), original.getUserIdentityTokens(),
				original.getTransportProfileUri(), original.getSecurityLevel());
	}

	// Connect the client to server, not opc ua defined
	public static boolean connect() {
		if (client == null) {
			logger.warn("client= " + client + ", create opc ua client before trying to connect");
			return false;
		} else {
			try {
				client.connect().get();
				connected = true;
				return true;
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
		}
		return false;
	}

	// Check whether client is connected or not
	public static boolean isConnected() {
		return connected;
	}

	// Disconnect the client from server
	public static boolean disconnect() {
		if (connected) {
			try {
				client.disconnect().get();
				return true;
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	// Method from Moritz
	public static NodeId translateBrowsePathToNodeId(String path, String ns, NodeId startNodeofPath) {
		
		int namespaceIndex = Integer.parseInt(ns);
		
		BrowsePathResult result;
		if(!path.equals("")) {
			try {
				String[] path_s = path.split("/");
				ArrayList<RelativePathElement> elems = new ArrayList<>(path_s.length);

				for (String p : path_s) {
					elems.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true,
							new QualifiedName(namespaceIndex, p)));
				}
				List<BrowsePath> paths = new ArrayList<>(1);
				RelativePathElement[] a = new RelativePathElement[path_s.length];
				paths.add(new BrowsePath(startNodeofPath, new RelativePath(elems.toArray(a))));

				TranslateBrowsePathsToNodeIdsResponse response = client.translateBrowsePaths(paths).get();

				result = response.getResults()[0];
				if (result == null || result.getTargets() == null || result.getTargets().length < 1) {
					logger.error("no strResult found for <\\" + path + "\\>\\");
				}

				return new NodeId(namespaceIndex, result.getTargets()[0].getTargetId().getIdentifier().toString());

			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			return null;
		} else {
			logger.warn("path= " + path);
		}
		return null;
	}

	// Browse Service, browse the node and return all subnodes
	public static List<Node> browse(String ns, String identifiertype, String identifier) {

		NodeId nodeId = convertNodeId(ns, identifiertype, identifier);
		
		if(nodeId != null) {
			try {
				return client.getAddressSpace().browse(nodeId).get();
			} catch (Exception e) {
				if (e.getMessage() != null) {
					logger.error(e.getMessage());
				}
			}
		} else {
			logger.error("nodeId= " + nodeId);
		}
		
		return null;
	}

	public static ReferenceDescription[] getReferencesOfNode(NodeId nodeId) {
		
		if(nodeId != null) {
			BrowseDescription browse = new BrowseDescription(
					nodeId,
					BrowseDirection.Both, 
					Identifiers.References,
					true,
					uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()
							| NodeClass.DataType.getValue() | NodeClass.Method.getValue()
							| NodeClass.ObjectType.getValue() | NodeClass.ReferenceType.getValue()
							| NodeClass.Unspecified.getValue() | NodeClass.VariableType.getValue()
							| NodeClass.View.getValue()),
					uint(BrowseResultMask.All.getValue()));

			try {			
				BrowseResult browseResult = client.browse(browse).get();
				return browseResult.getReferences();

			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
				return null;
			}
		} else {
			logger.error("nodeId= " + nodeId);
			return null;
		}
	}

	public static UaNode getNodeInstance(String ns, String identifierType, String identifier) {
		
		NodeId nodeId = convertNodeId(ns, identifierType, identifier);
		
		try {			
			return client.getAddressSpace().getNodeInstance(nodeId).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	public static CompletableFuture<ReadResponse> read(NodeId nodeId, int attributeId) {
		
		List<ReadValueId> readValueIds = new ArrayList<>();
		UInteger integer = uint(attributeId);
		
		ReadValueId readValue = new ReadValueId(nodeId, integer, null, null);
		readValueIds.add(readValue);
		
		return client.read(0, TimestampsToReturn.Server, readValueIds);
	}
	
	// Read service of the specified node
	public static String readValue(String ns, String identifiertype, String identifier) {

		NodeId nodeId = convertNodeId(ns, identifiertype, identifier);

		if(nodeId != null) {
			try {
				return Long.toString(client.readValue(0, TimestampsToReturn.Server, nodeId).get().getStatusCode().getValue());
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return null;
		} else {
			logger.error("nodeId= " + nodeId);
			return null;
		}
	}

	// Write service, write datavalue to the node
	public static String write(String ns, String identifiertype, String identifier, DataValue dataValue) {

		NodeId nodeId = convertNodeId(ns, identifiertype, identifier);

		if(nodeId != null) {
			try {
				return client.writeValue(nodeId, dataValue).get().toString();
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
			return null;
		} else {
			logger.error("nodeId= " + nodeId);
			return null;
		}
	}

	// User defined method, browse the node and compare it to the browsename, if it
	// is equal return the node
	public static boolean isSubnodeOf(String ns, String identifiertype, String identifier, String childBrowseName) {

		try {
			List<Node> nodes = OpcUaServicesMilo.browse(ns, identifiertype, identifier);
			for (Node node : nodes) {
				if ((node.getBrowseName().get().getNamespaceIndex() + ":" + node.getBrowseName().get().getName())
						.equals(childBrowseName)) {
					return true;
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	// User defined method, browse the node and compare it to the browsename, if it
	// is equal return the node
	public static NodeId getSubNodeByName(String ns, String identifiertype, String identifier, String childBrowseName) {

		try {
			List<Node> nodes = OpcUaServicesMilo.browse(ns, identifiertype, identifier);
			for (Node node : nodes) {
				if ((node.getBrowseName().get().getNamespaceIndex() + ":" + node.getBrowseName().get().getName())
						.equals(childBrowseName)) {
					return node.getNodeId().get();
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public static CompletableFuture<QualifiedName> getBrowseNameOfNode(NodeId nodeId) {
		
		CompletableFuture<QualifiedName> qualifiedName = null;
		
		try {
			qualifiedName = client.getAddressSpace().getNodeInstance(nodeId).get().getBrowseName();
			
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
//			e.printStackTrace();
		}
		return qualifiedName;
	}

	// User definied methode to create a subscription
	public StatusCode subscription(NodeId nodeId, String valueString, NodeId valueNodeId) {
		StatusCode result = null;
		try {

			// create a subscription @ 1000ms
			UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

			// subscribe to the Value attribute of the server's CurrentTime node
			ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

			// important: client handle must be unique per item
			UInteger clientHandle = uint(clientHandles.getAndIncrement());

			MonitoringParameters parameters = new MonitoringParameters(clientHandle, 1000.0, // sampling interval
					null, // filter, null means use default
					uint(10), // queue size
					true // discard oldest
			);

			MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting,
					parameters);

			// when creating items in MonitoringMode.Reporting this callback is where each
			// item needs to have its
			// value/event consumer hooked up. The alternative is to create the item in
			// sampling mode, hook up the
			// consumer after the creation call completes, and then change the mode for all
			// items to reporting.
			BiConsumer<UaMonitoredItem, Integer> onItemCreated = (item, id) -> item
					.setValueConsumer(this::onSubscriptionValue);

			List<UaMonitoredItem> items = subscription
					.createMonitoredItems(TimestampsToReturn.Both, newArrayList(request), onItemCreated).get();

			for (UaMonitoredItem item : items) {
				if (item.getStatusCode().isGood()) {
					logger.info("item created for nodeId={}", item.getReadValueId().getNodeId());
					result = item.getStatusCode();
				} else {
					logger.warn("failed to create item for nodeId={} (status={})", item.getReadValueId().getNodeId(),
							item.getStatusCode());
					result = item.getStatusCode();
				}
			}

			// let the example run and then terminate
			Thread.sleep(10000);
			return result;
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		return result;
	}

	private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		logger.info("Pay attention value changed!!! value: item={}, value={}", item.getReadValueId().getNodeId(),
				value.getValue());
	}

	public static NamespaceTable getNamespaceTable() {
		return client.getNamespaceTable();
	}
	
	// User defined method to convert a nodeid as a string to a Milo nodeid
	static NodeId convertNodeId(String ns, String identifierType, String identifier) {
		
		NodeId nodeid = null;
		
		if(!ns.equals("") && !identifierType.equals("") && !identifier.equals("")) {
			int namespaceIndex = Integer.parseInt(ns);
				
			switch (identifierType) {
			case "Numeric":
				nodeid = new NodeId(namespaceIndex, UInteger.valueOf(identifier));
				break;
			case "String":
				nodeid = new NodeId(namespaceIndex, identifier);
				break;
			case "Guid":
				nodeid = new NodeId(namespaceIndex, UUID.fromString(identifier));
				break;
			case "Opaque":
				
				String numbers[] = identifier.substring(1, identifier.length() - 2).split(",");
				
				byte[] byteArray = new byte[numbers.length];
				
				for (int i = 0; i < byteArray.length; i++) {
					
					if (numbers[i].contains(" ")) {
						numbers[i] = numbers[i].substring(1, numbers[i].length());
					}
					
					Integer number = Integer.parseInt(numbers[i]);
					
					byteArray[i] = number.byteValue();
				}
				
				ByteString byteString = null;
				byteString = ByteString.of(byteArray);
				
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
