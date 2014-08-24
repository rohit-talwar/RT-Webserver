package adobe.WebServer;

import java.util.ArrayList;
/**
 * When the sig-int signal is given this class runs and terminates all the server objects created on this jvm
 * @author rohtalwa
 *
 */
public class GracefulExitHook extends Thread {
		
		private static ArrayList<Server> serverList = new ArrayList<Server>() ;
		
		/**
		 * Adds a server to the list of running servers 
		 * @param server a new server
		 */
		public static void addServer(Server server) {
			serverList.add(server) ;
		} 
		@Override
		public void run()
		{
			
			System.out.println("Shutdown hook ran!");
			for(Server s : serverList) {
				s.stopServer();
			}
			
			serverList.clear();
		}
	
}
