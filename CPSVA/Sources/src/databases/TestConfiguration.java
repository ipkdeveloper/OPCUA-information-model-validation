/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is a container class.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package databases;

public class TestConfiguration {

	private String testSystem;
	private String guid;
	private String ip;
	private String port;
	private String security;
	private String nodeStructure;
	private String variable;
	private String framework;

	public TestConfiguration() {
		this.testSystem = "";
		this.guid = "";
		this.ip = "";
		this.port = "";
		this.security = "";
		this.nodeStructure = "";
		this.variable = "";
	}

	public TestConfiguration(String testSystem, String guid, String ip, String port, String security,
			String nodestructure, String variable, String framework) {
		this.testSystem = testSystem;
		this.guid = guid;
		this.ip = ip;
		this.port = port;
		this.security = security;
		this.nodeStructure = nodestructure;
		this.variable = variable;
		this.framework = framework;
	}

	public String getTestSystem() {
		return testSystem;
	}

	public void setTestSystem(String testSystem) {
		this.testSystem = testSystem;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	public String getNodestructure() {
		return nodeStructure;
	}

	public void setNodestructure(String nodeStructure) {
		this.nodeStructure = nodeStructure;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}
}
