/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is capable of loading and storing xml files.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package externalFiles;

import java.io.File;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import databases.TestConfiguration;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;

public class LoadStore {
	
	private static Logger logger = LogManager.getLogger(LoadStore.class);
	
	public static UANodeSet loadInformationModel(String file) {
		
		if (!file.equals("")) {
			File inputFile = new File(file);
			UANodeSet uaNodeSet = null;
			
			try {
				JAXBContext inputJaxbContext = JAXBContext.newInstance(UANodeSet.class);
				Unmarshaller jaxbUnmarshaller = inputJaxbContext.createUnmarshaller();
				uaNodeSet = (UANodeSet) jaxbUnmarshaller.unmarshal(inputFile);
			} catch (JAXBException e) {
				logger.error(e.getMessage());
				JOptionPane.showMessageDialog(null, "Error, while load specification.");
			}
			
			return uaNodeSet;
		} else {
			logger.info("Error no specification selected");
			JOptionPane.showMessageDialog(null, "Error, no specification selected.");
			return null;
		}
		
	}
	
	public static void storeInformationModel(UANodeSet uaNodeSet, String file) {
		
		if(!file.equals("")) {
			File outputFile = new File(file);

			try {

				JAXBContext outputJaxbContext = JAXBContext.newInstance(UANodeSet.class);
				Marshaller jaxbMarshaller = outputJaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				jaxbMarshaller.marshal(uaNodeSet, outputFile);

			} catch (JAXBException e) {
				logger.error(e.getMessage());
				JOptionPane.showMessageDialog(null, "Error, while load specification.");
			}
		} else {
			logger.info("Error no specification selected");
			JOptionPane.showMessageDialog(null, "Error, no specification selected.");
		}

	}
	
	public static TestConfiguration loadTestConfig(String file) {
		
		if (!file.equals("")) {

			TestConfiguration testConfig = new TestConfiguration();

			String testSystem = "";
			String guid = "";
			String ip = "";
			String port = "";
			String security = "";
			String nodestructure = "";
			String variable = "";
			String framework = "";

			try {

				// Load xml into program
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(file);
				document.getDocumentElement().normalize();

				// UANodeSet ist the root element; Get all childs of root
				NodeList xmlElements = document.getElementsByTagName("testconfiguration").item(0).getChildNodes();

				for (int i = 1; i < xmlElements.getLength(); i++) {

					if(xmlElements.item(i).getNodeType() == Element.ELEMENT_NODE) {

						switch (xmlElements.item(i).getNodeName()) {
						case "testsystem":
							testSystem = xmlElements.item(i).getTextContent();
							break;

						case "guid":
							guid = xmlElements.item(i).getTextContent();
							break;

						case "ip":
							ip = xmlElements.item(i).getTextContent();
							break;

						case "port":
							port = xmlElements.item(i).getTextContent();
							break;

						case "security":
							security = xmlElements.item(i).getTextContent().toLowerCase().replaceAll("\\s", "");

							if (security.equals("none") || security.equals("no")) {
								security = "none";
							} else if (security.equals("basic128rsa15")) {
								security = "basic128rsa15";
							} else if (security.equals("basic256")) {
								security = "basic256";
							} else if (security.equals("basic256sha256")) {
								security = "basic256sha256";
							} else {
								security = "empty";
								logger.error("unkown security = " + security);
							}

							break;

						case "framework":
							framework = xmlElements.item(i).getTextContent();

							if (framework.contains("milo") || framework.contains("eclipse") || framework.contains("eclipsemilo")) {
								framework = "milo";
							} else if (framework.contains("opcf") || framework.contains("foundation")
									|| framework.contains("legacy")) {
								framework = "opcf";
							} else {
								framework = "empty";
								logger.error("unkown framework selected, choose Eclipse Milo or OPCF Java Legacy");
							}

							break;
						default:
							logger.warn("unkown xml element name");
							break;
						}
					}
				}

				testConfig = new TestConfiguration(
						testSystem, guid, 
						ip, port, 
						security, nodestructure,
						variable, framework);

			} catch (Exception e) {
				logger.error(e.getMessage());
				JOptionPane.showMessageDialog(null, "Error, while loading testconfiguration. See LogFile.json for details.");
			}
			return testConfig;
		} else {
			logger.info("Error testconfiguration is empty.");
			JOptionPane.showMessageDialog(null, "Error, no specification selected.");
			return null;
		}
	}

}
