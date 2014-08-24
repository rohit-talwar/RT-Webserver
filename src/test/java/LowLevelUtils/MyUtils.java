package LowLevelUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import static org.junit.Assert.*;

public class MyUtils {
	
	/**
	 * 
	 * @return the generic connection string which needs to be added to all requests
	 */
	public String genericHeader(){
		return " HTTP/1.1 \r\n" + 
				"Host: localhost \r\n" + 
				"Date: " + new Date() + "\r\n" +
				"Connection: keep-alive\r\n" ;
	}
	/**
	 * Sends HEAD request for the provided filename to the server
	 * @param fileName : the name of file
	 * @param out : the output stream of the client's socket connection
	 * @return true if the request was sent successfully else false
	 */
	public boolean sendHEADRequest(String fileName, BufferedOutputStream out){
		String entity;
		try {
			entity = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			//fail("COuld not encode file name to utf-8 url");
			return false ;
			//e2.printStackTrace();
		}
		String request = "HEAD /" + entity + genericHeader() + "\r\n";
		//System.out.println(request);
		byte[] requestHeader = null;
		try {
			requestHeader = request.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			try {
				//log.info("Sending utf 8 encoded string as iso-8959-1 could not be sent");
				requestHeader = request.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				fail("could not convert it to either utf 8 or iso-8859-1") ;
				return false ;
			}
		}
		try {
			out.write(requestHeader) ;
			out.flush() ;
			//System.out.println("Hello 3");

		} catch (IOException e) {
			fail("Socket was broken");
			return false ;
		}
		return true ;
	}
	/**
	 * Sends the request for deletion of the provided filename to the server
	 * @param fileName : the name of file to be deleted
	 * @param out : the output stream of the client's socket connection
	 * @return true if the request was sent successfully else false
	 */
	public boolean sendDELETERequest(String fileName, BufferedOutputStream out){
		String entity;
		try {
			entity = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			fail("COuld not encode file name to utf-8 url");
			return false ;
			//e2.printStackTrace();
		}
		String request = "DELETE /" + entity + genericHeader() + "\r\n";
		//System.out.println(request);
		byte[] requestHeader = null;
		try {
			requestHeader = request.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			try {
		//		log.info("Sending utf 8 encoded string as iso-8959-1 could not be sent");
				requestHeader = request.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				fail("could not convert it to either utf 8 or iso-8859-1") ;
				return false ;
			}
		}
		try {
			out.write(requestHeader) ;
			out.flush() ;
			//System.out.println("Hello 3");

		} catch (IOException e) {
			fail("Socket was broken");
			return false ;
		}
		return true ;
	}
	/**
	 * Sends the request of the provided filename to the server
	 * @param fileName : the filename desired
	 * @param out : the output stream of the client's socket connection
	 * @return true if the request was sent successfully else false
	 */
	public boolean sendGetRequest(String fileName, BufferedOutputStream out){
		String entity;
		try {
			entity = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			//log.error("COuld not encode file name to utf-8 url");
			return false ;
			//e2.printStackTrace();
		}
		String request = "GET /" + entity + genericHeader() + "\r\n";
		//System.out.println(request);
		byte[] requestHeader = null;
		try {
			requestHeader = request.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			try {
		//		log.info("Sending utf 8 encoded string as iso-8959-1 could not be sent");
				requestHeader = request.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				fail("could not convert it to either utf 8 or iso-8859-1") ;
				return false ;
			}
		}
		try {
			out.write(requestHeader) ;
			out.flush() ;
			//System.out.println("Hello 3");

		} catch (IOException e) {
			fail("Socket was broken");
			return false ;
		}
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return true ;
	}
	
	/**
	 * Gets/Reads the response data from the socket stream and converts it using the encoding scheme provided
	 * @param reader : the connection's input stream reader
	 * @return the response headers received
	 */
	public String getResponseHeaders(MyRWUtils reader) {
		InputStream in = reader.getInputStream() ;
		try {
			while(in.available()==0)	;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(in.available()==0)
				return "" ;
		} catch (IOException e) {
			fail("Client's Socket broken - connection lost -- connection must be closed from the server's side");
		}
		String response = "";
		String line = null ;
		while( true ){										// creates a new string object every time -- not good for big files!
			line = reader.readLine() ;				// creates a new string object every time -- not good for big files!
			if(line == null ) {
				// nothing is present on the input stream!
				// TODO integrate a retry mechanism in this tester but out of scope of the problem
				// how much time the client needs to wait before it sends a new request!
				fail("Nothing on the input stream");
				break ;				// no need to read further -- this fails the test
			}
			//System.out.println("Line is " + line);
			if(line.equals("\r\n"))
				break ;
			response += line  ;
		}
		//System.out.println("Response is - " + response + " Length " + response.length());
		return response ;
	}
	/**
	 * FOR POST METHOD
	 * Gets/Reads the response data from the socket stream and converts it using the encoding scheme provided
	 * @param reader : the connection's input stream reader
	 * @return the response headers received
	 */
	public String getResponse(MyRWUtils reader) {
		InputStream in = reader.getInputStream() ;
		try {
			while(in.available()==0)	;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		try {
			if(in.available()==0)
				return "" ;
		} catch (IOException e) {
			fail("Client's Socket broken - connection lost -- connection must be closed from the server's side");
		}
		String response = "";
		String line = null ;
		while( true ){										// creates a new string object every time -- not good for big files!
			line = reader.readLine() ;				// creates a new string object every time -- not good for big files!
			if(line == null ) {
				// nothing is present on the input stream!
				// TODO integrate a retry mechanism in this tester but out of scope of the problem
				// how much time the client needs to wait before it sends a new request!
				fail("Nothing on the input stream");
				break ;				// no need to read further -- this fails the test
			}
			//System.out.println("Line is " + line);
			if(line.equals("\r\n")){
				line = reader.readLine() ;
				break ;
			}
			response += line  ;
		}
		//System.out.println("Response is - " + response + " Length " + response.length());
		//log.info("TEST Response received - "+ response);
		return response ;
	}
	/**
	 * Gets the value of a field - key in the present request
	 * @param field the field or key name to fetch (-- includes every char before the value to be taken)
	 * @param delim the cjhar which separates the value from other elements in the string
	 * @return the value of the field present between the given field and delim
	 */
	public String getField(String req, String field, char delim){
		int mark = -1 ;
		mark = req.indexOf(field) ;
		mark += field.length() ; 
		String value = "" ;
		while(req.charAt(mark) != delim ){
			value += req.charAt(mark) ;
			++mark ;
		}
		return value ;
	}
	
}
