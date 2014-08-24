package adobe.WebServer;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import LowLevelUtils.MyRWUtils;
import LowLevelUtils.MyUtils;
import LowLevelUtils.StartServer;
/**
 * Checks if Delete works or not
 * Creates a test file in the server's directory and tests if the file was deleted successfully
 * 
 * @author rohtalwa
 *
 */
public class DELETEBasic {
	private Server server ;
	private Socket client ;
	private String fileName ;
	private InputStream in ;
	private BufferedOutputStream outToServer ;
	private String encoding ;
	private String defaultHome ;
	private int port ;
	private MyUtils utils ;
	private File tmp ;
	/**
	 * Makes a server object and instantiates it with default values
	 * initiates a client connection to the server initalises the input stream of this class
	 */
	@Before
	public void beforeClass() {
		fileName = "temp.txt" ;			// creating a new file with this name
		encoding = "ISO-8859-1" ;
		defaultHome = "DefaultHome" ;
		utils = new MyUtils() ;
		port  = 6666 ;
		StartServer s = new StartServer(defaultHome , port) ;
		server = s.getServer() ;
		port = server.getPort() ;		// might change at time of launch due to socket bind exceptions
		new Thread( s ).start();
		try {
			client = new Socket("localhost" , server.getPort());
		} catch (UnknownHostException e1) {
			fail("Server was not started or is unable to accept connections");
		} catch (IOException e1) {
			fail("Socket connection broke");
		}
		outToServer = null;
		in = null ;
		try {
			in = client.getInputStream() ;
			outToServer = new BufferedOutputStream( client.getOutputStream() );
		} catch (IOException e) {
			fail("Cannot initialise output stream");
		}
		tmp = new File(server.getHomePath() + File.separator + fileName) ;
		if(!tmp.exists()){
			try {
				assertTrue("Temp file could not be created -- not proceeding further", tmp.createNewFile() );
			} catch (IOException e) {
				fail("Temp file could not be created -- not proceeding further");
			}
		}
	}
	
	
	/**
	 * Stops/closes the server and closes the client socket
	 * 
	 */
	@After
	public void tearDown() {
		if(tmp.exists())	tmp.delete() ;
		try {
			outToServer.flush();
			client.close();
		} catch (IOException e) {
			fail("Could not close the client socket") ;
		}
		server.stopServer(); 
		
	}
	@Test
	public void testDELETE() {
		Boolean sendSuccessful =  utils.sendDELETERequest(fileName, outToServer) ;
		assertEquals("Request could not be sent", true , sendSuccessful);
		String response = null ;
		if(sendSuccessful){
			MyRWUtils reader = new MyRWUtils(in, encoding) ;
			response = utils.getResponseHeaders(reader) ;
			//System.out.println(reader);
			//log.info("Server's Response " + response);
			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
			if(code.equals("200")){
					assertFalse("File should have been deleted", tmp.exists()) ;
			}else if(code.equals("403")){
				if(tmp.exists()){
					if(tmp.isFile() && tmp.canExecute()){
						fail("Server shows file not present or no delete permissions, whereas it does exist and we can execute on it");
					}
				}
			}else{
				fail("Internal server error!!") ;
			}
		}
	}

}
