/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is responsbile for logging.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package logfilemanagement;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import databases.org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;
import externalFiles.LoadStore;

public class LogFileManagement {
	
	private static String location = "de";
	private static Logger logger = LogManager.getLogger(LogFileManagement.class);
	private static String logFileName = "LogFile";

	// Every difference between spec and server is counted
	private static long differences = 0;

	public static void incrementDiff() {
		differences += 1;
	}

	public static void setLocation(String location) {
		
		LogFileManagement.location = location;
		
		logger.info(addErrorCode("Info010") + "src/resources/Log4j2.properties");
		logger.info(addErrorCode("Info016") + location);
	}

	public static Logger getLogger() {
		return logger;
	}

	public static String addErrorCode(String errorCode) {
		
		if (errorCode.startsWith("Bad")) {
			differences += 1;
		}
		
		String baseName = "resources.ErrorCodes";
		Locale locale = new Locale(location);

		try {
			ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
			return " " + errorCode + ": " + bundle.getString(errorCode);
		} catch (MissingResourceException e) {
			
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Error, while getting error code. See LogFile.json for further details.");
			return null;
		}
	}
	
	public static void storeInformationModel(UANodeSet uaNodeSet, String file) {
		LoadStore.storeInformationModel(uaNodeSet, file);
	}
	
	public static String getLogFileName() {
		return logFileName;
	}

	public static void openLogFiles(String pathToLogFile) {
		
		if (pathToLogFile != null && !pathToLogFile.equals("")) {

			try {
				
				logger.warn(addErrorCode("Warn003") + differences);
				
				pathToLogFile = pathToLogFile.replace("/", "\\");
				String dir = System.getProperty("user.dir");

				pathToLogFile = "\\" + pathToLogFile + logFileName;
				
				if ((System.getProperty("os.name").toLowerCase()).contains("windows")) {
					
					LogManager.shutdown();
					
					Runtime.getRuntime().exec(
							"java -jar DeviationAnalyse.jar -f " + dir + pathToLogFile + ".json",
							null,
							new File(dir + "\\DeviationAnalyse"));
				}

			} catch (IOException e) {
				logger.error(e.getMessage());
				JOptionPane.showMessageDialog(null, "Error, while opening log files. See LogFile.json for details.");
			}

		} else {
			logger.info("Error path to jsonlog is empty.");
			JOptionPane.showMessageDialog(null, "Error, while opening log files. See LogFile.json for details.");
		}

	}

}
