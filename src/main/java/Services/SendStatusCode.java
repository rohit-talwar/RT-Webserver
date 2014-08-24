package Services;
import java.io.*; 
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Makes a generic response headers to be sent to client
 * @author rtalwar
 *
 */
public class SendStatusCode {
	private static Logger log = Logger.getLogger(SendStatusCode.class.getName() ) ;
	/**
	 * Generates a valid http response string
	 * @param status http status code
	 * @param length content length
	 * @return string containing a http response header with values of date, content length and content type
	 */
	public static String genericHeader(String status, int length){
		Date now = new Date( );
		String header = "HTTP/1.1 " + status +  "\r\n" 
				 + "Server: RT-HTTP \r\n"
				+ "Date: " + now + "\r\n" 
				+  "Content-length: " + length + "\r\n"
				+ "Connection: keep-alive\r\n"
				 + "Content-type: text/html\r\n\r\n"; 
		return header ;
	}
	/**
	 * Sends a given string to the client socket's raw output stream
	 * @param response the string/message to be sent
	 * @param raw client's output stream
	 */
	public static void sendMessage(String response, OutputStream raw){
		try {
			byte[] responseHeader = response.getBytes("ISO-8859-1");
			raw.write(responseHeader);
			raw.flush();
			//OutputStream outToClient = new BufferedOutputStream( raw );
			//outToClient.write(responseHeader) ;
			//outToClient.flush() ;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (IOException io){
			log.error(" Error message not sent IO Exception Reason - Connection lost/broken " 
					+ io.getMessage() ) ;
			//io.printStackTrace() ;
		}
	}
	/**
	 * Send message with status 200  -- Meaning -- Ok
	 * @param raw client's output stream
	 */
	public static void send200(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>200 operation committed successfully</h1> </body> </html>" ) ;
		String header = genericHeader("200 Ok", body.getBytes().length ) ;
		header += body ;
		log.info(" 200 response sent" ) ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 403  -- Meaning -- Forbidden
	 * @param raw client's output stream
	 */
	public static void send403(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>403 Document Found but server cannot read the requested FILE, lack of permissions</h1> </body> </html>" ) ;
		String header = genericHeader("403 Forbidden", body.getBytes().length ) ;
		header += body ;
		log.info(" 403 response sent" ) ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 404  -- Meaning -- Not found
	 * @param raw client's output stream
	 */
	public static void send404(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>404 DOCUMENT NOT FOUND</h1> </body> </html>" ) ;
		String header = genericHeader("404 Not Found", body.getBytes().length ) ;
		header += body ;
		log.info(" 404 response sent " ) ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 400  -- Meaning -- Bad Request
	 * @param raw client's output stream
	 */
	public static void send400(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>400 Bad Request</h1> </body> </html>") ;
		String header = genericHeader("400 Bad Request", body.getBytes().length ) ;
		header += body;
		log.info(" 400 response sent  " ) ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 405  -- Meaning -- Method Not Allowed
	 * @param raw client's output stream
	 */
	public static void send405(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>405 Method Not Allowed</h1> </body> </html>" ) ;
		String header = genericHeader("405 Method Not Allowed", body.getBytes().length ) ;
		header += body ;
		log.info(" 405 response sent") ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 414  -- Meaning -- request URI too long
	 * @param raw client's output stream
	 */
	public static void send414(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>414 Request-URI Too Long</h1> </body> </html>" ) ;
		String header = genericHeader("414 Request-URI Too Long", body.getBytes().length ) ;
		header += body;
		log.info(" 404 response sent") ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 401  -- Meaning -- Unauthorized/Need authentication 
	 * @param raw client's output stream
	 */
	public static void send401(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>401 Unauthorized</h1> </body> </html>") ;
		String header = genericHeader("401 Unauthorized", body.getBytes().length ) ;
		header += body ;
		log.info(" 401 response sent") ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 500  -- Meaning -- Internal error
	 * @param raw client's output stream
	 */
	public static void send500(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>500 Internal Server Error</h1> </body> </html>") ;
		String header = genericHeader("500 Internal Server Error", body.getBytes().length ) ;
		header += body ;
		log.info(" 500 response sent ") ;
		sendMessage(header, raw) ;
	}
	/**
	 * Send message with status 204  -- Meaning -- No response
	 * @param raw client's output stream
	 */
	public static void send204(OutputStream raw){
		String body = new String(" <!DOCTYPE html> <html> <body> <h1>204 No Content - File uploaded/deleted succesfully</h1> </body> </html>") ;
		String header = genericHeader("204 No Content", body.getBytes().length ) ;
		header += body ;
		log.info(" 204 response sent ") ;
		sendMessage(header, raw) ;
	}
	
}
