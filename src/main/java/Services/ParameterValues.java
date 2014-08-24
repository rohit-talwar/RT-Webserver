package Services;

/*
 * This class initializes the config file with the values 
 * which can be changed later on from the config file itself
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Instantiates the config file at a folder config and file with name config.properties
 * @author rtalwar
 *
 */

public class ParameterValues {

	/**
	 * Generates the conf.properties file in the conf folder with default values of
	 * <pre>
	 * "Core Pool Size", "2"
	 * "Maximum Pool Size", "50"
	 * "port", "6666"
	 * "Keep Alive Time", "5000"
	 * "homeDirectory", "DefaultHome"
	 * "Socket Backlog", "50"
	 * "Maximum request length", "1024"
	 * "Favicon File Name", "favicon.ico"
	 * "Upload File Directory", "DefaultUpload"
	 * </pre>
	 * @param configFileName - the name of the default file to be created
	 * @return true if the config file is created with default values else returns false
	 */
	public static Boolean generateDefaultParameters(String configFileName) {
		Properties serverProperties = new Properties() ;
		OutputStream configFile = null ;
		try{
			configFile = new FileOutputStream(configFileName) ;
			serverProperties.setProperty("Core Pool Size", "3" ) ;
			serverProperties.setProperty("Maximum Pool Size", "50" ) ;
			serverProperties.setProperty("port", "6666") ;
			serverProperties.setProperty("homeDirectory", "DefaultHome") ;
			serverProperties.setProperty("Keep Alive Time", "5000") ;
			serverProperties.setProperty("Socket Backlog", "50") ;
			serverProperties.setProperty("Maximum request length", "1024") ;
			serverProperties.setProperty("Favicon File Name", "favicon.ico") ;
			serverProperties.setProperty("Upload File Directory", "DefaultHome" + File.separator + "DefaultUpload") ;
			serverProperties.storeToXML(configFile, "Core and Maximum pool size is the number of Threads to have in the thread pool, " +
					"port - port server listening " +
					"homeDirectory - this is the directory to be served" +
					"Keep Alive Time (in milliseconds) - the time after an idle thread dies") ; 
		}catch(IOException io){
				io.printStackTrace() ;
				return false ;				// Parameter File couldnt be formed!
		} finally {
			if(configFile != null){
				try{
					configFile.close() ;
				}catch (IOException io){
					io.printStackTrace() ;
				}
			}
		}
		return true ;
	}

}
