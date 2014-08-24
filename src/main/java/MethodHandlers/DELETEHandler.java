package MethodHandlers;
import java.io.DataInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import Services.SendStatusCode;
import adobe.WebServer.Server;


/**
 * Serves the delete requests coming to the server
 * @author rohtalwa
 *
 */
public class DELETEHandler extends MethodHandler {
	
	private static Logger log = Logger.getLogger(DELETEHandler.class.getName() ) ;
	/**
	 * 
	 * @param in the Delete request
	 * @param ds : the input stream of the client connection -- the stream to read requests from
	 * @param raw the output stream going to the client
	 * @param server the server object where this request came
	 */
	public DELETEHandler(String in, DataInputStream ds, OutputStream raw, Server server){
		super(in, ds, raw, server) ;
	}
	/**
	 * Deletes the file given in the request and sends appropriate response
	 */
	public void serveRequest(){
		String lPath = server.getHomePath() ;
		int i=7 ;									// IMP - where DELETE finishes
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
		log.info("File to be Deleted - " + localPath ) ;
		
//		if(localPath.equals(server.getHomePath()+"/") 
//				|| localPath.equals(server.getHomePath()+ "/index.html") ){
//			SendStatusCode.send403(raw); 
//			return ; 	// cannot delete index file 
//		}
			
		File askedFile = new File(localPath) ;
		if(askedFile.exists()){
			if(askedFile.isFile()){
				if(askedFile.canExecute()){
					if(askedFile.delete()) {
						SendStatusCode.send200(raw) ;
					}else{
						SendStatusCode.send500(raw) ;
 					}
				}else{
					SendStatusCode.send403(raw) ;		// unauthorized to delete file
				}
			}else{
				SendStatusCode.send403(raw) ;			// cannot delete a folder 
			}
		}else{
			SendStatusCode.send200(raw) ;				// cannot delete a file not present
		}
	}
	
}
