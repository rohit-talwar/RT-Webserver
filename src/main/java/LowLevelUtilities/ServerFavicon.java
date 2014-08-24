package LowLevelUtilities;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import Services.SendStatusCode;
import adobe.WebServer.Server;
/**
 * Every server will have a favicon file associated with it. This class serves that favicon
 * @author rohtalwa
 *
 */
public class ServerFavicon {
	private String fileName ;
	private static Logger log = Logger.getLogger(ServerFavicon.class.getName() ) ;
	private Boolean fileLoaded ;
	private byte[] data ;
	private Server server ;
	/**
	 * tests is the favicon file loaded
	 * @return true if the file is loaded or not, false if file could not be loaded(-- in this case send 404 file not found error) 
	 */
	public Boolean getFileLoaded() {
		return fileLoaded;
	}
	/**
	 * 
	 * @return returns the favicon file name
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * 
	 * @param fileName sets the file name of the favicon of this server
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * Sends the favicon as set in the server
	 * @param raw the client connection's output stream which requested the favicon
	 */
	public void send(OutputStream raw) {
		log.info("Favicon asked!") ;
		if(fileLoaded){						// checks if the favicon file is loaded into the object
			Date now = new Date( );
			String header = "HTTP/1.1 200 OK\r\n" 
					+ "Server: RT-HTTP\r\n"
					+ "Date: " + now + "\r\n"
					+ "Connection: close\r\n"
					+ "Content-length: " + data.length + "\r\n"
					+ "Content-type: image/x-icon\r\n\r\n" ;
			log.debug("Sending Favicon " + header);
			try{
				byte[] responseHeader = header.getBytes("ASCII") ;
				OutputStream outToClient = new BufferedOutputStream( raw );
				outToClient.write(responseHeader) ;
				outToClient.write(data) ;
				outToClient.flush() ;
				log.info("Favicon Sent!") ;
			}catch (IOException io){
				log.error("Couldnt not send favicon - connection closed");
			}
		}else{
			SendStatusCode.send404(raw) ;
		}
	}
	/**
	 * @param server : the server that has this favicon object
	 * @param fileName : the favicon file of this server
	 */
	public ServerFavicon (Server server , String fileName){
		this.fileName = fileName ;
		fileLoaded = false ;
		data = null ;
		this.server = server ;
	}
	/**
	 * Loads the favicon file as given in the server's root directory into internal data structure, if file not present loads the 
	 * default favicon present as Default.ico in the DefaultHome Directory
	 * 
	 */
	public void loadFavicon(){
		File askedFile = new File(server.getHomePath()+File.separator + fileName ) ;
		if(!askedFile.exists() || !askedFile.isFile() || !askedFile.canRead()){
			log.error("The input file is corrupted/missin/not readable, switching to default favicon") ;
			fileName = "DefaultHome" + File.separator + "DefaultFavicon.ico" ; 				// switch the file name to default as the set file does not exist
			askedFile = new File(fileName) ;
		}		
		if(askedFile.exists() && askedFile.isFile() && askedFile.canRead()){
			try {
				InputStream in = new FileInputStream(askedFile); 			// reading the file from disk 
				ByteArrayOutputStream o = new ByteArrayOutputStream( ); 
				int b; 
				while ((b = in.read( )) != -1) o.write(b); 
				in.close();
				data = o.toByteArray( );
				fileLoaded = true ;
				log.info("Favicon File " +server.getHomePath()+File.separator+ fileName + " loaded into server ") ;
			} catch (IOException io) {
				fileLoaded = false ;
				log.error("Favicon File " +server.getHomePath()+File.separator+ fileName + " could not be read " ) ;
			}
		}else{
			fileLoaded = false ;
			log.error("Could not load favicon file");
		}
	}
}
