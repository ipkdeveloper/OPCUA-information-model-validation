/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file checks a given xml file against a xsd file.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package externalFiles;

import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

public class ConsistencyCheck {
	
	private static Logger logger = LogManager.getLogger(ConsistencyCheck.class);

	public static boolean validateXmlAgainstXsd(String xmlFile, String xsdFile) {

		if (!xmlFile.equals("") || !xsdFile.equals("")) {
			
			logger.error("no xml or xsd file selected, xml file = " + xmlFile + ", xsd file = " + xsdFile);
			return false;
		}
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory
					.newSchema(new StreamSource(System.getProperty("user.dir") + "/XSD/" + xsdFile));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(xmlFile));
			logger.info("validation of xml file against xsd finished, no errors");
			return true;
		} catch (IOException | SAXException e) {
			
			logger.error(e.getMessage());
			return false;

		}
	}
}
