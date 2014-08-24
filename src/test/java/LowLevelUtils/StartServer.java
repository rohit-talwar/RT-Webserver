package LowLevelUtils;

import adobe.WebServer.Server;

/**
 * Starts the server for testing can be loaded with values specified else default values are utilized
 * 
 * @author rohtalwa
 *
 */
public class StartServer implements Runnable {
	private Server server ;
	
	public Server getServer(){
		return server ;
	}
	
	
	public StartServer (int port, String uploadDir){
		server = new Server(port, uploadDir);
	}
	
	public StartServer(String homePath, int port){
		server = new Server(homePath, port) ;
	}
	
	public StartServer(String homePath, int port, String uploadDir){
		server = new Server(homePath, port, uploadDir) ;
	}
	
	public StartServer(int port) {
		server = new Server(port) ;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		server.startServer(); 
	}
	
	
}
