package adobe.WebServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import LowLevelUtils.MyRWUtils;
import LowLevelUtils.MyUtils;
import LowLevelUtils.StartServer;



/**
 * @author rohtalwa
 *
 */
public class POSTBasic {
	private Server server ;
	private Socket client ;
	private String fileName ;
	private InputStream in ;
	private OutputStream out ;
	private String encoding ;
	private String defaultUpload ;
	private int port ;
	private MyUtils utils ;
	private MyRWUtils reader ;
	private String defaultHome ;
	private String fileUploadPath ;
	private String fileSentPath ; 
	/**
	 * Makes a server object and instantiates it with default values
	 * initiates a client connection to the server initializes the input stream of this class
	 */
	@Before
	public void beforeClass() {
		fileName = "index.html" ;
		encoding = "ISO-8859-1" ;
		defaultUpload = "DefaultHome" + File.separator + "DefaultUpload" ;
		defaultHome = "DefaultHome" ;
		fileUploadPath = defaultUpload + File.separator + fileName ;			// the place where the file will be uploaded by the server -- product of this request
		fileSentPath = defaultHome + File.separator + fileName ;				// the place where the file resides right now
		File tmp = new File(fileUploadPath) ;
		if(tmp.exists()){
			tmp.delete() ;
		}
		utils = new MyUtils() ;
		port  = 6666 ;
		StartServer s = new StartServer(defaultHome, port, defaultUpload) ;
		server = s.getServer() ;
		assertEquals("Please make default home folder", server.getHomePath(), defaultHome);
		assertEquals("Please make default upload folder inside default home folder", server.getUploadPath(), defaultUpload ) ;
		new Thread( s ).start();
		try {
			client = new Socket("localhost" , server.getPort());
		} catch (UnknownHostException e1) {
			fail("Server was not started or is unable to accept connections");
		} catch (IOException e1) {
			fail("Socket connection broke");
		}
		out = null;
		in = null ;
		try {
			in = client.getInputStream() ;
			out = client.getOutputStream() ;
		} catch (IOException e) {
			fail("Cannot initialise output stream");
		}
		reader = new MyRWUtils(in, encoding) ;
	}
	
	
	/**
	 * Stops/closes the server and closes the client socket
	 * 
	 */
	@After
	public void tearDown() {
		try {
			out.flush();
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
	public void testPOST(){
		boolean sendSuccessful =  reader.sendPOSTRequest(fileName, out, fileSentPath) ;
		assertEquals("Request could not be sent", true , sendSuccessful);
		boolean rcvSuccessful = false ;
		String response = null ;
		if(sendSuccessful){
			response = utils.getResponse(reader) ;
			//System.out.println(reader);
			//log.info("Server's Response " + response);
			String code = utils.getField(response, "HTTP/1.1 ", ' ') ;
			//System.out.println("Received code " + code );
			if(code.equals("200")){
				try {
			//		System.out.println("File Comparsion executed");
					rcvSuccessful = reader.isFileBinaryEqual(new File(fileUploadPath), new File(fileSentPath) ) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					fail("Could not check the validity of the uploaded file");
					rcvSuccessful = false ;
				}
			}else {
				rcvSuccessful = false ;
			}
		}
		assertEquals("Document was not received completely", true , rcvSuccessful);
	}
}

