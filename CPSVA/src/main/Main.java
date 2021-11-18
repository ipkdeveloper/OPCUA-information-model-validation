/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file is the main class.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package main;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	
	public static void main(String[] args) {		
		
		JOptionPane.showMessageDialog(null, "Start of validation. Press enter to continue");
		
		String adapterConfigPath = "";
		
		if (args.length != 0) {
			adapterConfigPath = args[0];
		} else {
			adapterConfigPath = "AdapterConfig.properties";
		}
		
		JOptionPane.showMessageDialog(null, "Load AdapterConfig.properties. Press enter to continue");

		Batch.loadAdapterConfig(adapterConfigPath);
		
		final Logger logger = LogManager.getLogger(Main.class);
		
		Batch.initializeLogFileManagement();
		
		JOptionPane.showMessageDialog(null, "Initialize LogFile management. Press enter to continue");

		Batch.startCheck();
		
		logger.info(Batch.addErrorCode("Info009"));

		Batch.openLogFiles();
		
	}
}