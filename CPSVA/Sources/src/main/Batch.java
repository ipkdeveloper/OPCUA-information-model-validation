/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is the batch file.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Validation;

public class Batch {

	private static String language = null;
	private static String testConfiguration = null;
	private static String xmlFile = null;
	private static String xmllogPath = null;
	private static String jsonLogPath = null;
	
	private static Logger logger = null;

	public String getLanguage() {
		if(Batch.language == null) {
			logger.info("Language = " + language);
			Batch.language = "";
		}
		return Batch.language;
	}

	public void setLanguage(String language) {
		Batch.language = language;
	}

	public String getTestConfiguration() {
		if(Batch.testConfiguration == null) {
			logger.info("testConfiguration = " + Batch.testConfiguration);
			Batch.testConfiguration = "";
		}
		return Batch.testConfiguration;
	}

	public void setTestConfiguration(String testConfiguration) {
		Batch.testConfiguration = testConfiguration;
	}

	public String getXmlFile() {
		if(Batch.xmlFile == null) {
			logger.info("xmlFile = " + Batch.xmlFile);
			Batch.xmlFile = "en";
		}
		return Batch.xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		Batch.xmlFile = xmlFile;
	}

	public String getXmlPathLogs() {
		if(Batch.xmllogPath == null) {
			logger.info("xmlLogPath = " + Batch.xmllogPath);
			Batch.xmllogPath = "en";
		}
		return Batch.xmllogPath;
	}

	public void setXmlPathLogs(String xmllogPath) {
		Batch.xmllogPath = xmllogPath;
	}

	public String getJsonPathLogs() {
		if(Batch.jsonLogPath == null) {
			logger.info("jsonLogPath = " + Batch.jsonLogPath);
			Batch.jsonLogPath = "";
		}
		return Batch.jsonLogPath;
	}

	public void setJsonPathLogs(String jsonLogPath) {
		Batch.jsonLogPath = jsonLogPath;
	}

	public static void loadAdapterConfig(String adapterConfigPath) {

		Properties properties = new Properties();
		
		if (!(adapterConfigPath == null || adapterConfigPath.equals(""))) {
			
			try {

				BufferedInputStream stream = new BufferedInputStream(new FileInputStream(adapterConfigPath));
				properties.load(stream);
				stream.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error, could not find AdapterConfig! See LogFile.json for details");
			}
			
			String logConfig = null;
			if (properties.getProperty("log4jPath") != null) {
				logConfig = properties.getProperty("log4jPath");
			} else {
				JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig!");
			}
			String executionDir = System.getProperty("user.dir").replace("\\", "\\\\");
			System.setProperty("log4j.configurationFile",  executionDir + "//" + logConfig + "Log4j2.properties");
			
			if (properties.getProperty("JsonLogPath") != null) {
				jsonLogPath = properties.getProperty("JsonLogPath");
			}
			else {
				JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig! See LogFile.json for details");
			}
			File oldJsonLogFile = new File(executionDir + "//" + jsonLogPath + "/Logfile.json");
			if (!oldJsonLogFile.delete()) {
				JOptionPane.showMessageDialog(null, "Could not delete old Logfile.json.");
			}
						
			if (properties.getProperty("Language") != null) {
				language = properties.getProperty("Language");
			} else {
				logger.error("AdapterConfig.properties: language is missing");
				JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig! See LogFile.json for details");
			}
			if (properties.getProperty("TestConfiguration") != null) {
				testConfiguration = properties.getProperty("TestConfiguration");
			}
			 else {
					logger.error("AdapterConfig.properties: TestConfiguration is missing");
					JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig! See LogFile.json for details");
				}
			if (properties.getProperty("XmlFile") != null) {
				xmlFile = properties.getProperty("XmlFile");
			}
			 else {
					logger.error("AdapterConfig.properties: XmlFile is missing");
					JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig! See LogFile.json for details");
				}
			if (properties.getProperty("XmlLogPath") != null) {
				xmllogPath = properties.getProperty("XmlLogPath");
			}
			 else {
					logger.error("AdapterConfig.properties: XmlLogPath is missing");
					JOptionPane.showMessageDialog(null, "Error, while reading the AdapterConfig! See LogFile.json for details");
				}

			logger = LogManager.getLogger(Batch.class);
			
		} else {
			JOptionPane.showMessageDialog(null, "Error, could not find AdapterConfig.properties!");
		}		

	}

	public static String getLogFileName() {
		return Validation.getLogFileName();
	}

	public static void initializeLogFileManagement() {
		Validation.initializeLogFileManagement(language);
	}
	
	public static String addErrorCode(String errorCode) {
		return Validation.addErrorCode(errorCode);
	}
	
	public static void startCheck() {

		if (!(language.equals(""))) {
			if (!(testConfiguration.equals(""))) {
				if (!(xmlFile.equals(""))) {
					if (!(xmllogPath.equals(""))) {
						if (!(jsonLogPath.equals(""))) {
							Validation.startValidation(language, testConfiguration, xmlFile, xmllogPath);
						} else {
							logger.error(Validation.addErrorCode("Error011"));
							JOptionPane.showMessageDialog(null, "Error, while starting the validation process! See LogFile.json for details");
						}
					} else {
						logger.error(Validation.addErrorCode("Error010"));
						JOptionPane.showMessageDialog(null, "Error, while starting the validation process! See LogFile.json for details");
					}
				} else {
					logger.error(Validation.addErrorCode("Error002"));
					JOptionPane.showMessageDialog(null, "Error, while starting the validation process! See LogFile.json for details");
				}
			} else {
				logger.error(Validation.addErrorCode("Error001"));
				JOptionPane.showMessageDialog(null, "Error, while starting the validation process! See LogFile.json for details");
			}
		} else {
			logger.error(Validation.addErrorCode("Warn001"));
			JOptionPane.showMessageDialog(null, "Error, while starting the validation process! See LogFile.json for details");
			
		}

	}

	public static void openLogFiles() {
		Validation.openLogFiles(xmllogPath);
	}
}
