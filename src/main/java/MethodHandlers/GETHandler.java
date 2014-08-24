package MethodHandlers;
import java.net.*; 
import java.io.*; 
import java.util.*;

import org.apache.log4j.Logger;

import Services.SendStatusCode;
import adobe.WebServer.Server;

/**
 * Implementation of the GET method - Handles/Serves get requests 
 * Sends back the content type, content length and the content of the file requested if authorized resource is found to exist
 * @author rtalwar
 * 
 */

public class GETHandler extends MethodHandler {
	private static Logger log = Logger.getLogger(GETHandler.class.getName()) ;

	/**
	 * 
	 * @param in : the get request server received
	 * @param ds : the input stream of the client connection -- the stream to read requests from
	 * @param raw : the output stream of the client connection -- the stream to write back at the client socket 
	 * @param server : the server to which the get request is called
	 */
	public GETHandler(String in, DataInputStream ds, OutputStream raw, Server server){
		super(in, ds, raw, server) ;
	}
	
	/**
	 * Actual function that serves the request 
	 */
	@Override
	public void serveRequest() {
		long timeStart = System.currentTimeMillis() ;
		String lPath = server.getHomePath() ;
		int i=4 ;	// where get finishes

		while(in.charAt(i)!=' ' ){
			if(in.charAt(i)=='?')
				break ;
			lPath += in.charAt(i) ;
			++i ;
		}


		String localPath = "";
		try {
			localPath = URLDecoder.decode(lPath,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("could not determine the file name from url " + lPath + " Stopping execution");
			return ;
		}
		//System.out.println("url decoded = " + localPath);
		log.info("File to be fetched - " + localPath ) ;
		if(localPath.equals(server.getHomePath()+"/favicon.ico") ) {	
			server.getFavicon().send(raw);
			return ;
		}
		File askedFile = new File(localPath) ;
		if(!askedFile.exists()){
			log.info("File " + localPath + " does not exist " ) ;
			SendStatusCode.send404(raw) ;
			return ;
		}
		if(askedFile.isDirectory()){
			//String prefix = "http://localhost:" + Integer.toString(server.getPort()) + "/" ;
			String prefix = "" ;
			String root = server.getHomePath().replaceAll("\\\\", "/") + "/" ;
			String message = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title> Welcome to the browse directory utility</title> </head> <body>" ;
			File files[] = askedFile.listFiles() ;
			for (File fl : files) {
				String fName = fl.getPath().replaceAll("\\\\", "/") ;
				try {
					fName = fName.split(root)[1] ;
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				try {
					//System.out.println(fName);
					message += "<br> <a href=\"" +(prefix + URLEncoder.encode(fName,"UTF-8")) + "\" >" + fName + "</a> <br>"  ;
					//System.out.println("URL " + fName);
				} catch (UnsupportedEncodingException e) {
				}
			}
			message += "</body> </html>" ;
			byte[] responseBody = null;
			try {
				responseBody = message.getBytes("ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
				log.error("could not send response -- unable to convert to the ISO-8859-1 charset");
			}
			Date now = new Date( );
			String header = "HTTP/1.1 200 OK\r\n" 
					+ "Server: RT-HTTP\r\n"
					+ "Date: " + now + "\r\n"
					+ "Connection: keep-alive\r\n"
					+ "Content-length: " + responseBody.length + "\r\n"
					+ "Content-type: text/html\r\n\r\n" ;
			try {
				byte[] responseHeader = header.getBytes("ISO-8859-1");
				raw.write(responseHeader) ; 
				raw.write(responseBody) ;
				raw.flush(); 
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
				log.error("could not send response -- unable to convert to the ISO-8859-1 charset");
			}catch ( IOException e){
				log.error("socket broken - could not send directory listing");
			}
			
		}else if(askedFile.isFile() && askedFile.canRead()){
			try {
				Date now = new Date( );
				String header = "HTTP/1.1 200 OK\r\n" 
						+ "Server: RT-HTTP\r\n"
						+ "Date: " + now + "\r\n"
						+ "Connection: keep-alive\r\n"
						+ "Content-length: " + askedFile.length() + "\r\n"
						+ "Content-type: " + URLConnection.getFileNameMap().getContentTypeFor(localPath) + "\r\n\r\n" ;
				log.debug("Response Headers " + header);
				byte[] responseHeader = header.getBytes("UTF-8") ;
				//OutputStream outToClient = new BufferedOutputStream( raw );
				raw.write(responseHeader) ;
				FileInputStream input = new FileInputStream(askedFile); 			// reading the file from disk
				int n = 0;
				byte[] buffer = new byte[4096] ;							// 4kb buffer size
				while (-1 != (n = input.read(buffer))) {					// -1 is End of File
					raw.write(buffer, 0, n);
				}
				input.close();
				raw.flush(); 
				log.debug("Sent full file of length " + askedFile.length() + " in time " + (System.currentTimeMillis() - timeStart) );
			} catch (IOException io) {
				log.error("File " + localPath + " could not be sent "  
						+ " IO Exception Reason - Connection lost/broken or file not read " ) ;
				// SendStatusCode.send500(raw) ;
				// io.printStackTrace() ;
			}
		}else{
			// Unable to read the file -- hence the file cannot be opened even if correct auth details are given
			// To view this file set the read permissions of this file (use chmod or run program from a privileged user account)
			SendStatusCode.send403(raw) ;
		}

	}
}
