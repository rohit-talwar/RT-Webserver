package MethodHandlers;
import java.net.*; 
import java.io.*; 
import java.util.*;

import org.apache.log4j.Logger;

import Services.SendStatusCode;
import adobe.WebServer.Server;
/**
 * Implementation of the HEAD method -- this fetches the content size and last modified information of the resource as requested,
 * @author rtalwar
 *
 */
public class HEADHandler extends MethodHandler {
	private static Logger log = Logger.getLogger(HEADHandler.class.getName()) ;
	/**
	 * 
	 * @param in : the get request server received
 	 * @param ds : the input stream of the client connection -- the stream to read requests from
 	 * @param raw : the output stream of the client connection -- the stream to write back at the client socket 
 	 * @param server : the server object that is serving the request
	 */
	public HEADHandler(String in, DataInputStream ds, OutputStream raw, Server server){
		super(in, ds, raw, server) ;
	}
	/**
	 * Actual function that serves the request 
	 */
	public void serveRequest() {	
		String lPath = server.getHomePath() ;
		int i= 5;										// where head finishes
		while(in.charAt(i)!=' '){
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
		log.info("File to be fetched - " + localPath  ) ;
		if(localPath.equals(server.getHomePath()+"/")) localPath += "index.html" ; 	// sending the index file
		File askedFile = new File(localPath) ;
		if(askedFile.exists()){
			if(askedFile.isFile()){
				if(askedFile.canRead()){
					try {
						Date now = new Date( );
						String header = "HTTP/1.1 200 OK\r\n" 
								 + "Server: RT-HTTP \r\n"
								+ "Date: " + now + "\r\n"
								+ "Connection: keep-alive\r\n"
								 + "Content-length: " + askedFile.length() + "\r\n"
								 + "Last-Modified: " + askedFile.lastModified() + "\r\n" 
								 + "Content-type: " + URLConnection.getFileNameMap().getContentTypeFor(localPath) + "\r\n\r\n";		
						byte[] responseHeader = header.getBytes("UTF-8") ;
						raw.write(responseHeader) ;
						raw.flush();
						//OutputStream outToClient = new BufferedOutputStream( raw );
						//outToClient.write(responseHeader) ;
						//outToClient.flush() ;
					} catch (IOException io) {
						SendStatusCode.send500(raw) ;
						log.debug(io.getMessage()) ;
					}
				}else{
					SendStatusCode.send404(raw) ;
				}
			}else{
				log.info("File " + localPath + " is not a file"  ) ;
				SendStatusCode.send403(raw) ;
			}
		}else{
			log.info("File " + localPath + " does not exist " ) ;
			SendStatusCode.send404(raw) ;
		}

	}
}
