package MethodHandlers;

import java.io.DataInputStream;
import java.io.OutputStream;

import adobe.WebServer.Server;
/**
 * The abstract class that every method the server provides has to have
 * @author rohtalwa
 *
 */
public abstract class MethodHandler {
	OutputStream raw ;
	String in ; 
	Server server ;
	DataInputStream ds ;
	/**
	 * The details of the client - server connection which is necessary for serving any request 
	 * @param in : the get request server received
 	 * @param ds : the input stream of the client connection -- the stream to read requests from
 	 * @param raw : the output stream of the client connection -- the stream to write back at the client socket 
 	 * @param server : the server object that is serving the request
	 */
	public MethodHandler(String in, DataInputStream ds, OutputStream raw, Server server){
		this.in = in ;
		this.server = server ;
		this.ds = ds ;
		this.raw = raw ;
	}
	/**
	 * The function used to service a request pertaining a particular function
	 */
	public abstract void serveRequest() ;
	
}
