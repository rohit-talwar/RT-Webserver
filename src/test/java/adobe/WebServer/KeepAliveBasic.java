//package adobe.WebServer;
//
//import static org.junit.Assert.*;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.Socket;
//import java.net.UnknownHostException;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import LowLevelUtils.MyRWUtils;
//import LowLevelUtils.MyUtils;
//import LowLevelUtils.StartServer;
//
//public class KeepAliveBasic {
//	private Server server ;
//	private Socket client ;
//	private String fileName ;
//	private InputStream in ;
//	private BufferedOutputStream outToServer ;
//	private String encoding ;
//	private String defaultHome ;
//	private int port ;
//	private MyUtils utils ;
//	private File tmp ;
//	private String tmpfileName ;
//	/**
//	 * Makes a server object and instantiates it with default values
//	 * initiates a client connection to the server initalises the input stream of this class
//	 */
//	@Before
//	public void beforeClass() {
//		fileName = "index.html" ;
//		encoding = "ISO-8859-1" ;
//		defaultHome = "DefaultHome" ;
//		utils = new MyUtils() ;
//		port  = 6666 ;
//		StartServer s = new StartServer(defaultHome , port) ;
//		server = s.getServer() ;
//		new Thread( s ).start();
//		try {
//			client = new Socket("localhost" , server.getPort());
//		} catch (UnknownHostException e1) {
//			fail("Server was not started or is unable to accept connections");
//		} catch (IOException e1) {
//			fail("Socket connection broke");
//		}
//		outToServer = null;
//		in = null ;
//		try {
//			in = client.getInputStream() ;
//			outToServer = new BufferedOutputStream( client.getOutputStream() );
//		} catch (IOException e) {
//			fail("Cannot initialise output stream");
//		}
//		// creating file for the delete method
//		tmpfileName = "temp.txt" ;
//		tmp = new File(server.getHomePath() + File.separator + tmpfileName) ;
//		if(!tmp.exists()){
//			try {
//				assertTrue("Temp file could not be created -- not proceeding further", tmp.createNewFile() );
//			} catch (IOException e) {
//				fail("Temp file could not be created -- not proceeding further");
//			}
//		}
//	}
//	
//	
//	/**
//	 * Stops/closes the server and closes the client socket
//	 * 
//	 */
//	@After
//	public void tearDown() {
//		if(tmp.exists())	tmp.delete() ;
//		try {
//			outToServer.flush();
//			client.close();
//		} catch (IOException e) {
//			fail("Could not close the client socket") ;
//		}
//		server.stopServer(); 
//		
//	}
//	/**
//	 * tests the keep alive behavior by running the basic get post delete and head request using the same socket connection
//	 * @return 
//	 */
//	@Test
//	public void testKeepAlive(){
//		
//		System.out.println("testing head");
//		testHEAD();
//		System.out.println("testing delete");
//		testDELETE();
//		System.out.println("testing get");
//		testGet();
//	}
//	/**
//	 * Tests the get function and check if it can get a basic file -- index.html
//	 */
//	public void testGet(){
//		//System.out.println("Hello");
//		Boolean sendSuccessful =  utils.sendGetRequest(fileName, outToServer) ;
//		assertEquals("Request could not be sent", true , sendSuccessful);
//		Boolean rcvSuccessful = false ;
//		String response = null ;
//		String filePath = server.getHomePath() + File.separator + fileName ;
//		if(sendSuccessful){
//			MyRWUtils reader = new MyRWUtils(in, encoding) ;
//			response = utils.getResponseHeaders(reader) ;
//			//System.out.println(reader);
//			//log.info("Server's Response " + response);
//			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
//			long contentLength = Long.parseLong(utils.getField(response, "Content-length: " , '\r')) ;
//			//System.out.println("IN TEST content length received " + contentLength);
//			if(code.equals("200")){
//				try {
//					rcvSuccessful = reader.checkBytes(filePath, contentLength) ;
//				} catch (IOException e) {
//					fail("Socket Connection broke while receiving data") ;
//				}
//			}else if(code.equals("404")){
//				// file not found
//				rcvSuccessful = !(new File(filePath).isFile()) ;
//			}else {
//				rcvSuccessful = false ;
//			}
//		}
//		assertEquals("Document was not received completely", true , rcvSuccessful);
//	}
//	
//	/**
//	 * Tests the get function and check if it can get a basic file -- index.html
//	 */
//	public void testHEAD(){
//		//System.out.println("Hello");
//		Boolean sendSuccessful =  utils.sendHEADRequest(fileName, outToServer) ;
//		assertEquals("Request could not be sent", true , sendSuccessful);
//		String response = null ;
//		String filePath = server.getHomePath() + File.separator + fileName ;
//		File test = new File(filePath) ;
//		long fileLength = test.length() ;
//		if(sendSuccessful){
//			MyRWUtils reader = new MyRWUtils(in, encoding) ;
//			response = utils.getResponseHeaders(reader) ;
//			//System.out.println(reader);
//		//	log.info("Server's Response " + response);
//			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
//			long contentLength = Long.parseLong(utils.getField(response, "Content-length: " , '\r')) ;
//			//System.out.println("IN TEST content length received " + contentLength);
//			if(code.equals("200")){
//				assertEquals(contentLength, fileLength);
//			}else if(code.equals("404")){
//				assertFalse("Server cant see this file, but it exists",!test.exists() ) ;
//			}else {
//				fail("Internal Error at the server -- no clue why !");
//			}
//		}
//	}
//	/**
//	 * Test the delete method by creating a file temp.txt and then deleting it
//	 */
//	public void testDELETE() {
//		Boolean sendSuccessful =  utils.sendDELETERequest(tmpfileName, outToServer) ;
//		assertEquals("Request could not be sent", true , sendSuccessful);
//		String response = null ;
//		if(sendSuccessful){
//			MyRWUtils reader = new MyRWUtils(in, encoding) ;
//			response = utils.getResponseHeaders(reader) ;
//			//System.out.println(reader);
//			//log.info("Server's Response " + response);
//			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
//			if(code.equals("200")){
//					assertFalse("File should have been deleted", tmp.exists()) ;
//			}else if(code.equals("403")){
//				if(tmp.exists()){
//					if(tmp.isFile() && tmp.canExecute()){
//						fail("Server shows file not present or no delete permissions, whereas it does exist and we can execute on it");
//					}
//				}
//			}else{
//				fail("Internal server error!!") ;
//			}
//		}
//	}
//	
//	
//}
