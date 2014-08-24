package LowLevelUtilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.text.Utilities;

import org.apache.log4j.Logger;

import Services.ParameterValues;
import adobe.WebServer.Server;
/**
 * This is a utility class that helps in reading data byte data from the underlying stream
 * It reuses the buffers which are created only once when the class is initialized, and also reads byte data from
 * the stream converting it to string/chars when needed and as specified 
 * @author rohtalwa
 *
 */
public class Utilitiies {
	private static Logger log = Logger.getLogger( ( Utilities.class.getName() ) )  ;
	private byte[] buffer ;
	private int bufferCapacity ;
	private InputStream in ;
	private int start ;
	/**
	 * Initializes the reader configurations, sets the buffer size and the input streams for data input
	 * @param bufferCapacity the buffer capacity for reading data
	 * @param in the client connection's input stream where the data is pending to be read  
	 */
	public void readInit(int bufferCapacity, InputStream in){
		this.bufferCapacity = bufferCapacity ;
		this.in = in ;
		buffer = new byte[bufferCapacity] ;
		start = 0;
//		cur = 0;
	}
	/**
	 * Reads size bytes from the stream and writes it to the file with specified name
	 * @param rcvdFile : the file object to write to 
	 * @return 0 if all bytes were written successfully 1 if the file could not be created or 2 if there was some IO error 
	 * 
	 */
	public int rwXBytes(File rcvdFile, long size) {
		int realCapacity = bufferCapacity ;
		BufferedOutputStream writeToFile = null;
		try {
			writeToFile = new BufferedOutputStream(new FileOutputStream(rcvdFile));
			int count = 0 ;
			while(size >= bufferCapacity && count != -1 && bufferCapacity > 64){
				count = in.read(buffer, 0, bufferCapacity) ;
				size -= count ;
				//System.out.println("This is my " + count);
				writeToFile.write(buffer, 0, count);
				while(size < bufferCapacity && bufferCapacity > 64 ){ 
					bufferCapacity /= 2 ; 
				//	System.out.println("BUffer Size reduced to " + bufferCapacity);
				}
			}
			//System.out.println("So the final size left to read is - "+ size + " last buffer size was " + bufferCapacity);
			for(int i=0; i<size; ++i){		// the most heavy step in the whole program
				int a = in.read() ;
				writeToFile.write(a);
			}
			writeToFile.flush();
			writeToFile.close();
		} catch (FileNotFoundException e) {
			log.error("File not found or cannot be created");
			return 1 ;
		} catch (IOException e) {
			log.error("Writing to file was interrupted unexpectedly");
			return 2 ;
		}
		bufferCapacity = realCapacity ;				// reinitializing the buffercapacity
		return 0 ;
	}
	/**
	 * Reads a line from the input and returns the number of bytes read breaks reading from new line
	 * 
	 * @param encoding : the encoding which is is used to encode the bytes
	 * @return MyLine object containing the the total number of bytes read and the encoded string obtained from these bytes 
	 * Special Cases for the getBytes in the MyLine object
	 * Returns -1 if nothing was read and there was an error!
	 * Returns 0 if new line was read   
	 * Returns -2 if the line was greater than the buffer size but buffer sized line is returned (May be garbled -- data not to be relied on if -2)
	 */
	public MyLine readLine(String encoding){
		start = -1;
		int a ;
		String s = null;
		int errorFlag = -1 ;		 			// default value of the error flag
		try {
			while( ( a = in.read() ) != -1 ){
				++start ;
				if(start >= bufferCapacity){		// erroneus string
					s = new String(buffer, 0, start, encoding) ;
					log.error("The buffer limit reached the string is being sent abruptly");
					errorFlag = -2 ;				// -2 will replace the bytes part 
					break ;
				}
				buffer[start] = (byte)a ;
				if((char)a == '\n' ){
					++start ;
					s = new String(buffer, 0, start, encoding) ;
					break ;
				}
//				if((char)a == '\r'){
//					a = in.read() ;			//reading the '\n' coming on the network
//					if((char)a != '\n')		System.out.println("ERRROOOOOOOOORRRR");
//					// convert byte buffer to string and send
//					if(start != 0){					// not an empty line
//						s = new String(buffer, 0, start, encoding) + " ";
//					}else{
//						System.out.println("SENDING EMPTY LINE");
//					}
//					break ;
//				}else{
//					if(start >= bufferCapacity){		// erroneus string
//						s = new String(buffer, 0, start, encoding) + " ";
//						log.error("The buffer limit reached the string is being sent abruptly");
//						errorFlag = -2 ;				// -2 will replace the bytes part 
//						break ;
//					}
//					buffer[start] = (byte) a ;					
//				}
			}
			if(s==null && start!=-1){
				s = new String(buffer, 0, start+1, encoding) ;		// say we dont get \r or \n but the eof is reached we still need to send the string so formed
			}
		} catch (IOException e) {
			log.error("Socket is broken cant read next byte from stream ");
			s = "Closed" ;
		}
		if(errorFlag != -1)	 					// errorflag was changed 
			start = errorFlag ;			
		MyLine line = new MyLine(s, start) ;
		//log.info("line object line - " + line.getLine() + " line bytes "+ line.getBytes()) ;
		return line ;
	}
	
	/**
	 * Tries to instantiate the server from the values provided in the config file, if that fails creates a 
	 * config file and loads the values from that file 
	 * @param server the server which to instantiate
	 * @param configFileName the config file containing the values
	 */
	public static void loadValues(Server server, String configFileName){
		if(!Utilitiies.loadValuesfromFile(configFileName, server)){
			configFileName += ".Default" ;
			ParameterValues.generateDefaultParameters(configFileName) ;			// generating a default file
			Utilitiies.loadValuesfromFile(configFileName, server) ;					// loading vcalues from the default file
		}
	}
	/**
	 * Loads the parameter values from the config file into the server object
	 * @param configFileName the path name of the config file 
	 * @return true if the file was successfully opened and used to draw parameters for the server, else false is returned
	 */
	public static Boolean loadValuesfromFile(String configFileName, Server server) {
		Properties serverProperties = new Properties() ;
		InputStream configFile = null ;
		configFileName = configFileName.replace( "/", File.separator) ;
		configFileName = configFileName.replace("\\", File.separator) ;
		File conf = new File(configFileName) ;
		Boolean loadSuccessful = true ;	
		if(( conf.exists() && conf.isFile() && conf.canRead() )){	
			try {
				boolean setProperty = false ;
				String failMessage = "Could not load value from file -- value is corrupted -- loading default value : " ;
				configFile = new FileInputStream(conf) ;
				serverProperties.loadFromXML(configFile) ;
				if(serverProperties.containsKey("homeDirectory")){
					setProperty = server.setHomePath(serverProperties.getProperty("homeDirectory") );
					if(setProperty){
						System.out.println("Loaded home path from file " + configFileName + " - value - " + server.getHomePath() );
						log.info("Loaded home path from file " + configFileName + " - value - " + server.getHomePath() );
					}else{
						log.info(failMessage + server.getHomePath() );
					}
				}
				if(serverProperties.containsKey("port")){
					setProperty = server.setPort(serverProperties.getProperty("port") );
					if(setProperty){
						System.out.println("Loaded port from file  " + configFileName + " - value - " + server.getPort());
					log.info("Loaded port from file  " + configFileName + " - value - " + server.getPort());
					}else{
						log.info(failMessage + server.getPort());
					}
				}
				if(serverProperties.containsKey("Core Pool Size")){
					setProperty = server.setCorePoolSize(serverProperties.getProperty("Core Pool Size"));
					if(setProperty){
						System.out.println("Loaded core pool size from file  " + configFileName + " - value loaded is - " + server.getCorePoolSize());
					log.info("Loaded core pool size from file  " + configFileName + " - value loaded is - " + server.getCorePoolSize());
					}else{
						log.info(failMessage + server.getCorePoolSize()); 
					}
				}
				if(serverProperties.containsKey("Maximum Pool Size")){
					setProperty = server.setMaxPoolSize(serverProperties.getProperty("Maximum Pool Size") ); 
					if(setProperty){
						System.out.println("Loaded max pool size from file  " + configFileName + " - value - " + server.getMaxPoolSize());
					log.info("Loaded max pool size from file  " + configFileName + " - value - " + server.getMaxPoolSize());
					}else{
						log.info(failMessage + server.getMaxPoolSize()) ;
					}
				}
				if(serverProperties.containsKey("Socket Backlog")){
					setProperty = server.setBacklog(serverProperties.getProperty("Socket Backlog") );
					if(setProperty){
						System.out.println("Loaded Socket Backlog from file  " + configFileName + " - value - " + server.getBacklog());
					log.info("Loaded Socket Backlog from file  " + configFileName + " - value - " + server.getBacklog());
					}else{
						log.info(failMessage + server.getBacklog()) ;
					}
				}
				if(serverProperties.containsKey("Keep Alive Time") ){
					setProperty = server.setKeepAliveTime( serverProperties.getProperty("Keep Alive Time") );
					if(setProperty){
						System.out.println("Loaded Keep alive time from file  " + configFileName + " - value - " + server.getKeepAliveTime());
					log.info("Loaded Keep alive time from file  " + configFileName + " - value - " + server.getKeepAliveTime());
					}else{
						log.info(failMessage + server.getKeepAliveTime()) ;
					}
				}
				if(serverProperties.containsKey("Maximum request length")){
					setProperty = server.setReqLength(serverProperties.getProperty("Maximum request length")  );
					if(setProperty){
						System.out.println("Loaded Maximum request length from file  " + configFileName + " - value - " + server.getReqLength() );
						log.info("Loaded Maximum request length from file  " + configFileName + " - value - " + server.getReqLength() );
					}else{
						log.info(failMessage + server.getReqLength() ) ;
					}
				}
				if(serverProperties.containsKey("Favicon File Name")){
					server.getFavicon().setFileName(serverProperties.getProperty("Favicon File Name") ); 
					log.info("Loaded Favicon File Name from file  " + configFileName + " - value - " + server.getFavicon().getFileName() );
				}
				if(serverProperties.containsKey("Upload File Directory")){
					setProperty = server.setUploadPath(serverProperties.getProperty("Upload File Directory") );
					if(setProperty){
						System.out.println("Loaded file upload dir from config file  " + configFileName + " - value - " + server.getUploadPath());
						log.info("Loaded file upload dir from config file  " + configFileName + " - value - " + server.getUploadPath() );
					}else{
						log.info(failMessage + server.getUploadPath()) ;
					}
				}
			}catch(IOException io) {
				io.printStackTrace() ;
				loadSuccessful = false;													// unable to load values from file
				log.error("Config File  " + configFileName + " is corrupted or major IO error --Default values in use! ");
			}finally{
				if(configFile != null ){
					try{
						configFile.close() ;
					}catch(IOException io){
						log.debug(io.getMessage());
						//io.printStackTrace() ;
					}
				}
			}
		}else{
			loadSuccessful = false ;
		}
		return loadSuccessful ;
	}

}
