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

public class HEADBasic {
	private Server server ;
	private Socket client ;
	private String fileName ;
	private InputStream in ;
	private BufferedOutputStream outToServer ;
	private String encoding ;
	private String defaultHome ;
	private int port ;
	private MyUtils utils ;
	/**
	 * Makes a server object and instantiates it with default values
	 * initiates a client connection to the server initalises the input stream of this class
	 */
	@Before
	public void beforeClass() {
		fileName = "index.html" ;
		encoding = "ISO-8859-1" ;
		defaultHome = "DefaultHome" ;
		utils = new MyUtils() ;
		port  = 6666 ;
		StartServer s = new StartServer(defaultHome , port) ;
		server = s.getServer() ;
		port = server.getPort() ;
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
		
	}
	
	
	/**
	 * Stops/closes the server and closes the client socket
	 * 
	 */
	@After
	public void tearDown() {
		try {
			outToServer.flush();
			client.close();
		} catch (IOException e) {
			fail("Could not close the client socket") ;
		}
		server.stopServer(); 
		
		
	}

	/**
	 * Tests the get function and check if it can get a basic file -- index.html
	 */
	@Test
	public void testHEAD(){
		//System.out.println("Hello");
		Boolean sendSuccessful =  utils.sendHEADRequest(fileName, outToServer) ;
		assertEquals("Request could not be sent", true , sendSuccessful);
		String response = null ;
		String filePath = server.getHomePath() + File.separator + fileName ;
		File test = new File(filePath) ;
		long fileLength = test.length() ;
		if(sendSuccessful){
			MyRWUtils reader = new MyRWUtils(in, encoding) ;
			response = utils.getResponseHeaders(reader) ;
			//System.out.println(reader);
		//	log.info("Server's Response " + response);
			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
			long contentLength = Long.parseLong(utils.getField(response, "Content-length: " , '\r')) ;
			//System.out.println("IN TEST content length received " + contentLength);
			if(code.equals("200")){
				assertEquals(contentLength, fileLength);
			}else if(code.equals("404")){
				assertFalse("Server cant see this file, but it exists",!test.exists() ) ;
			}else {
				fail("Internal Error at the server -- no clue why !");
			}
		}
	}

}
