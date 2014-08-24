package adobe.WebServer;


/**
 * A set of threads that serve the active connections/requests at the server
 * 
 * NOTE: Requests handled on basis of the RFC 2616 available at http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html
 * 
 */




import java.net.*; 
import java.io.*; 

import org.apache.log4j.Logger;

import LowLevelUtilities.Utilitiies;
import MethodHandlers.DELETEHandler;
import MethodHandlers.GETHandler;
import MethodHandlers.HEADHandler;
import MethodHandlers.MethodHandler;
import MethodHandlers.POSTHandler;
import Services.SendStatusCode;

public class ServerEngine implements Runnable{

	private Server server ;
	private Socket client ;
	private int count ;
	private static Logger log = Logger.getLogger(ServerEngine.class.getName() ) ;
	private BufferedOutputStream raw  ;
	private DataInputStream in ;
	public Utilitiies myreader ;
	private int bufferCapacity = 8096 ;
	/**
	 * initializes the server engine which processes each request incoming to the server
	 * @param server the server on which the request is coming
	 * @param client the socket connection containing the client's information
	 * @param count the connection reuse count
	 * @param polling interval - the time at which the socket should be polled again for checking any new request -- the connection will be closed if time exceeds the keep alive time 
	 */
	public ServerEngine(Server server, Socket client, int count){
		this.server = server ;
		this.client = client ;
		this.count = count ;
		in = null ;
		raw = null ;
		try {
			in = new DataInputStream(client.getInputStream() );
			raw = new BufferedOutputStream(client.getOutputStream());
		} catch (IOException e) {
			log.error("Socket got broken in constructor! no input can be taken");
		}
		myreader = new Utilitiies() ;
		myreader.readInit(bufferCapacity, in );
	}
	/**
	 * reads the request headers from the socket  
	 * @return the request received from the socket, in case the request is greater than max request length return empty string. 
	 * Returns "Closed" - indicates that this socket is either closed or input is shutdown  -- normally upon receiving this message closeConnection should be called  
	 */
	@SuppressWarnings("deprecation")
	public String readRequest(){
		String strReq = "";
		String line = null ;
		try {
			while((line = in.readLine())!= null ) {
				if(line.equals("")) break ;
				strReq += line + " ";
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("Request is - " + strReq + " Length " + strReq.length());
		//if(strReq.length() > server.getReqLength() ) 	return "" ;
		log.info("Request received - "+ strReq);
		return strReq ;
	}
	/**
	 * Closes the client socket connection attached to this server engine
	 */
	public void closeConnection() {
		if(!client.isClosed() ){
			try {
				log.debug("connection " + client.getInetAddress().toString() + " closed due to timeout connection use count - " + count ) ;
				client.close() ;
			} catch (IOException e) {
				log.error("exception at closing the connection / connection timeout - " + e.getMessage() );
			}
		}
	}
	@Override
	public void run() {
		Boolean GET = false ;
		Boolean POST = false ;
		Boolean DELETE = false ;
		Boolean HEAD = false ;
		Boolean KEEP_ALIVE = true ;
//		long keepTime = server.getKeepAliveTime() ;
//		long timeStart = System.currentTimeMillis() ;
//		while(server.getServerAccepts()){
			String strReq = "" ;  				// this is the parsed request
			strReq = readRequest() ;
			if(strReq.equals("Closed")){
				closeConnection(); 
				return ;
			}

			if(strReq.equals("")){
//				if((System.currentTimeMillis() - timeStart) > keepTime ){
					closeConnection() ;
					return ;					// stopping this thread!
//				}
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				continue ;
			}
//			timeStart = System.currentTimeMillis() ; 				// a new request has come
			if(!strReq.contains("HTTP")){
				SendStatusCode.send400(raw); 
			}
			// no validating the http request only check for some key flags
			GET = false ;
			POST = false ;
			DELETE = false ;
			HEAD = false ;
			if(strReq.contains("Connection")){
				if(strReq.contains("keep-alive") || strReq.contains("KEEP-ALIVE") || strReq.contains("Keep-Alive") ){
					KEEP_ALIVE = true ;
				}else{
					KEEP_ALIVE = false ;					// connection flag present but not keep alive -- therefore connection is closed
				}
			}else{
				if(strReq.contains("HTTP/1.1")){			// keep alive is default in http/1.1
					KEEP_ALIVE = true ;
				}else{
					KEEP_ALIVE = false ;
				}
			}
			if(KEEP_ALIVE) {
				try {
					client.setKeepAlive(true) ;
				} catch (SocketException e) {
					log.error("Could not set tcp keep alive as Socket got broken!! exiting thread - " + 
							"Stack trace " + e.toString()) ;
				}
			}
			MethodHandler handle = null ;
			
			GET = strReq.startsWith("GET ") ;
			if(GET) handle = new GETHandler(strReq, in, raw, server) ;
			
			POST = strReq.startsWith("POST ") ;
			if(POST) handle = new POSTHandler(strReq, in, raw, server) ;
			
			DELETE = strReq.startsWith("DELETE ");
			if(DELETE) handle = new DELETEHandler(strReq, in, raw, server) ;
			
			HEAD = strReq.startsWith("HEAD ");
			if(HEAD) handle = new HEADHandler(strReq, in, raw, server) ;
			
			if(GET || HEAD || DELETE || POST){
				handle.serveRequest();
			}else if(client.isConnected()){ 
					SendStatusCode.send405(raw) ;
			}
//		}
        closeConnection(); 
		return ;
	}
} 



