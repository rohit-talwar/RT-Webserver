package adobe.WebServer;

import java.io.File;

import LowLevelUtilities.Utilitiies;
/**
 * Starts the server with the given config file present in the conf/ directory
 * @author rohtalwa
 *
 */
public class Main {
	public static void main(String[] args) {
		Server serverObject = new Server() ;
		// Opens the config file and loads values from there
		String configFileName = "conf"+ File.separator + "config.properties" ;
		Utilitiies.loadValues(serverObject, configFileName);
//		System.out.println("Hello baby");
		// ADDING SHUTDOWN HOOK
		serverObject.startServer() ;
//		System.out.println("===========");
		

	}
}
