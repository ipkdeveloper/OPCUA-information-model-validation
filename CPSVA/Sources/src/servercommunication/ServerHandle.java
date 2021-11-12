/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file calls opc ua specific methods depending on the framework selection..
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */


package servercommunication;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import databases.org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;

public class ServerHandle {
	
	private static Logger logger = LogManager.getLogger(ServerHandle.class);

	private static String framework = "milo";

	public static String getFramework() {
		return framework;
	}

	public static void setFramework(String framework) {
		ServerHandle.framework = framework;
	}

	public static boolean createClient(String serverUrl, String port, String strSecurityPolicy) {

		try {
			if (framework.equals("milo")) {
				if(OpcUaMilo.createClient(serverUrl, port, strSecurityPolicy)) {
					return true;
				} else {
					return false;
				}
			} else if (framework.equals("opcf")) {
				if(OpcUaOPCF.createSession(serverUrl, port, strSecurityPolicy)) {
					return true;
				} else {
					return false;
				}
			} else {
				logger.error("unkown framework selected, choose Eclipse Milo or OPCF Java Legacy");
				JOptionPane.showMessageDialog(null, "Error, unknown framework selected. See LogFile.json for details.");
			}
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Error, while create OPC UA client. See LogFile.json for details.");
			return false;

		}
	}

	public static boolean connect() {

		if (framework.equals("milo")) {
			return OpcUaMilo.connect();
		} else if (framework.equals("opcf")) {
			return OpcUaOPCF.activateSession();
		} else {
			logger.error("unkown framework selected, choose Eclipse Milo or OPCF Java Legacy");
			JOptionPane.showMessageDialog(null, "Error, unknown framework selected. See LogFile.json for details.");
			return false;
		}
	}

	public static boolean disconnect() {

		if (framework.equals("milo")) {
			return OpcUaMilo.disconnect();
		} else if (framework.equals("opcf")) {
			return OpcUaOPCF.closeSession();
		} else {
			logger.error("unkown framework selected, choose Eclipse Milo or OPCF Java Legacy");
			JOptionPane.showMessageDialog(null, "Error, unknown framework selected. See LogFile.json for details.");
			return false;
		}
	}

	public static UANodeSet createServerInformationmodel() {

		if (framework.equals("milo")) {
			return OpcUaMilo.createServerInformationModel();
		} else if (framework.equals("opcf")) {
			return OpcUaOPCF.createServerInformationmodel();
		} else {
			logger.error("unkown framework selected, choose Eclipse Milo or OPCF Java Legacy");
			JOptionPane.showMessageDialog(null, "Error, unknown framework selected. See LogFile.json for details.");
			return null;
		}
	}
	

}
