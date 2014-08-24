package MethodHandlers;
import java.io.*; 
import java.net.URLEncoder;
import java.util.Date;

import org.apache.log4j.Logger;


import Services.SendStatusCode;
import adobe.WebServer.Server;

/**
 * Implementation of the POST method - Handles/Serves POST requests that are sent with a tag of multipart/form-data (standard protocol for uploading files)
 * Stores the incoming file in the server's home directory back the content type, content length and the content of the file requested if authorized resource is found to exist
 * @author rtalwar
 * 
 */

public class POSTHandler extends MethodHandler {
	private String boundary ;
	private static Logger log = Logger.getLogger(POSTHandler.class.getName()) ;
	private String fileName ;
	private long contentLength ;
	private String first ;
	private String second ;
	
	/**
	 * 
	 * @param in : the post request server received
	 * @param ds : the input stream of the client connection -- the stream to read requests from
	 * @param raw : the client connection' output stream  
	 * @param server : the server to which the get request is called
	 */
	public POSTHandler(String in, DataInputStream ds, OutputStream raw, Server server){
		super(in, ds, raw, server) ;
	}
	/**
	 * validates the incoming request checks for content type content length and fills/initializes 
	 * the int internal data structures to help further processing
	 * @return true if the request is a valid, else false
	 */
	public boolean validateRequest() {
		if(in.contains("multipart/form-data") && 
				(in.contains("Content-Length") || in.contains("Content length") || in.contains("Content Length") ) &&
						(in.contains("boundary") || in.contains("Boundary") )  ){
			return true ;
		}else{
			return false ;
		}	
	}
	/**
	 * Gets the value of a field - key in the present request
	 * @param req the parent string in which to fetch
	 * @param field the field or key name to fetch (-- includes every char before the value to be taken)
	 * @param delim the char which separates the value from other elements in the string
	 * @return the value of the field present between the given field and delim
	 */
	public String getField(String req, String field, char delim){
		int mark = -1 ;
		mark = req.indexOf(field) ;
		//System.out.println("string is - "+ req + " mark is " + mark);
		mark += field.length() ; 
		String value = "" ;
		while(req.charAt(mark) != delim && req.charAt(mark)!='\0'){
			if(mark >= req.length())
				break ;
			value += req.charAt(mark) ;
			++mark ;
		}
		return value ;
	}
	/**
	 * Gets the values of the content length and instantiates the vaues of the file name and content length etc
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public void fetchData() throws IOException{
//		MyLine line = reader.readLine("ISO-8859-1") ;	// boundary
//		for(int i=0; i<15; ++i){
//			System.out.println("::::" + line.getLine() + " Bytes read " + line.getBytes());
//			line = reader.readLine("ISO-8859-1") ;
//		}
		// getting boundary
//		boundary = getField(in, "boundary=", '\r') ;
//		contentLength = Long.parseLong(getField(in, "Content-Length: ", '\r')) ;
		
		boundary = getField(in, "boundary=", ' ') ;
		contentLength = Long.parseLong(getField(in, "Content-Length: ", ' ')) ;
		
		//System.out.println("boundary is " + boundary );
		String l = ds.readLine() ;
		//MyLine line = reader.readLine("ISO-8859-1") ;	// boundary
		//System.out.println("::::" + line.getLine() + " Bytes read " + line.getBytes());
		//contentLength -= 2*line.getBytes() ;				// we get this boundary twice
		contentLength -= 2*l.getBytes().length ;
		//System.out.println(" haha boundary - " + line.getLine());
		
		//line = reader.readLine("ISO-8859-1") ;
		l = ds.readLine() ;
		//contentLength -= line.getBytes() ;
		contentLength -= l.getBytes().length ;
		//System.out.println(" haha content type " + line.getLine() + " Bytes read " + line.getBytes() );
		//System.out.println("in fetch data " + l);
		//fileName = getField(line.getLine(),"filename=\"" , '"' ) ;
		fileName = getField(l, "filename=\"" , '"' ) ;
		log.info("New file request received with name - " + fileName );
		fileName = server.getUploadPath() + File.separator + fileName ;				// converting the name to absolute path name
		breakName() ;
//		line = reader.readLine("ISO-8859-1") ;			// this is the data type line
		l = ds.readLine() ;
		//contentLength -= line.getBytes() ;
		contentLength -= l.getBytes().length ;
		//System.out.println(" data type - " + line.getLine() +  " Bytes read " + line.getBytes());
		//contentLength -= 4 ; 			// the bytes in String tmp = "\r\n" <- appended at the end of the file body and "--" ;	
		contentLength -= 14 ;
		l = ds.readLine() ;
		if(!l.equals("")){
			log.error("No blank line!! this is violation of protocol");
			//System.out.println("NO blank line after post content - this is violation of protocol");
		}
		//line = reader.readLine("ISO-8859-1") ;	// blank line
		//contentLength -= line.getBytes() ;
		//if(!line.getLine().equals("\r\n") ) 	log.error("No blank line!! this is violation of protocol"); //System.out.println("No blank line!! this is violation of protocol");
		//System.out.println("Finally content lenght is to write is " + contentLength);
	}
	/**
	 * breaks the name of the file into two parts - first: the name and second: the extension 
	 */
	public void breakName(){
		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		first = tokens[0] ;
		second = tokens[1] ;
	}
	/**
	 * inserts new name to the file(by appending brackets and a number inside it
	 * @param num the version number of the file - starts with 1 
	 */
	public void getNewName(int num){
		fileName = first + "(" + Integer.toString(num) + ")." + second ;
	}
	/**
	 * Reads the content length data from the stream and writes it to the stream 
	 * @return 0 if the file written successfully
	 * 1 if the file could not be created at the given place signifying internal error due to permissions etc
	 * 2 if the line was not written successfully
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public int rwdata() throws IOException{
		
		File newResource = new File(fileName) ;
		int i=0 ;
		while (newResource.exists()){
			++i ;
			getNewName(i) ;
			newResource = new File(fileName) ;
		}
		//int err = reader.rwXBytes(newResource, contentLength) ;
		int bufferCapacity = 655356 ;
		byte[] buffer = new byte[655356] ;
		BufferedOutputStream writeToFile = null;
		try {
			writeToFile = new BufferedOutputStream(new FileOutputStream(newResource));
			int count = 0 ;
			while(contentLength >= bufferCapacity && count != -1 && bufferCapacity > 64 ){
				count = ds.read(buffer, 0, bufferCapacity) ;
				contentLength -= count ;
				//System.out.println("This is my " + count);
				writeToFile.write(buffer, 0, count);
				while(contentLength < bufferCapacity && bufferCapacity > 64 ){ 
					bufferCapacity /= 2 ; 
				//	System.out.println("BUffer Size reduced to " + bufferCapacity);
				}
			}
			//System.out.println("So the final size left to read is - "+ size + " last buffer size was " + bufferCapacity);
			for(i=0; i<contentLength; ++i){		// the most heavy step in the whole program
				int a = ds.read() ;
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
		// end checking! -- double checking 
		//MyLine line = reader.readLine("ISO-8859-1") ;			// reading the '\r\n' at the end of the request body (file body)
		String l = ds.readLine() ;
		//line = reader.readLine("ISO-8859-1") ;
		l = ds.readLine() ;
		//String endLine = "--" + boundary + "--\r\n" ;
		String endLine = "--" + boundary + "--" ;
		//System.out.println("Final line " + line.getLine());
		//System.out.println("Success- Final line " + l + " Should have matched with - " + endLine);
			
		//if(line.getLine().equals(endLine)){
		if(l.equals(endLine)){
			// System.out.println("Success- Final line " + line.getLine() + " Has bytes - "+ line.getBytes() + " Should have matched with - " + endLine);
		//	System.out.println("Success- Final line " + l + " Should have matched with - " + endLine);
			log.info("File Uploaded successfully - with name " + fileName) ;
			return 0 ;
		}else{
			//System.out.println("In error - Final line " + line.getLine() + " Has bytes - "+ line.getBytes() + " Should have matched with - " + endLine);
			log.error("Major flaw in logic for getting files!");
			return 1 ;
		}
	}
	
	
	/**
	 * Actual function that serves the request 
	 */
	@Override
	public void serveRequest() {
		if( !validateRequest() ) {
			SendStatusCode.send400(raw);
			return ;
		}
		try {
			fetchData() ;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			// e2.printStackTrace();
			log.error("error in fetching data from using data inout stream");
		}
		int i = 0;
		try {
			i = rwdata();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			// e2.printStackTrace();
			log.error("error while writing to file");
		}
		if(i==0) {
			// send 200
			//String prefix = "http://localhost:" + Integer.toString(server.getPort()) + "/" ;
			String prefix = "" ;
			String root = server.getHomePath().replaceAll("\\\\", "/") + "/" ;
			String message = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title> Welcome to the Upload directory </title> </head> <h2>Upload File Directory</h2> <body>" ;
			message += "<h1> File Uploaded successfully with name as - " + fileName + " </h1>" ;
			File askedFile = new File(server.getUploadPath()) ; 
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
		}
		if(i==1)	SendStatusCode.send500(raw);
		if(i==2){
			// close connection - or close the input stream as erroneous data might be read from now!
			// or do nothing
		}
		
	}
	
}
