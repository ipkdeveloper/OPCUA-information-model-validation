/* _________________________________________________________________________________________________________
 * Author: 		Tobias Phillip Wolff
 * Company: 	Fraunhofer Institute for Production Systems and Design Technology
 * Contact:		Frank-Walter Jaekel, frank-walter.jaekel@ipk.fraunhofer.de
 * Description:	This source file gets the test configuration out of the xml by using the method of another
 * 				class.
 * 
 * Last update:	03.02.2020
 * Review:		Vincent Happersberger
 * _________________________________________________________________________________________________________
 */

package externalFiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import databases.TestConfiguration;

public class TestConfigManagement {
	
	private static Logger logger = LogManager.getLogger(TestConfigManagement.class);

	public static TestConfiguration getTestConfig(String file) {
		return LoadStore.loadTestConfig(file);
	}

}
