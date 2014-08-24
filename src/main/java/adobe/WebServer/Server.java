package adobe.WebServer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import LowLevelUtilities.ServerFavicon;

/**
 * Server class initializes the different aspects to start a server such as home path, thread pool information and keep alive time
 * @author rtalwar 
 */
public class Server {
	private ServerSocket serverSocket ;
	private String homePath ; 
	private volatile boolean serverAccepts ;
	private int port ;
	private int corePoolSize ;
	private int maxPoolSize  ;
	private long keepAliveTime ;
	private File rootDir ;
//	private ConnectionManager connections ;
	private int backlog ;
	private ThreadPoolExecutor engine ;
	private static Logger log = Logger.getLogger(Server.class.getName()) ;
	private int reqLength ;
	private ServerFavicon favicon ;
	private String uploadPath ;
	
	/**
	 * 
	 * @return the server favicon object associated with this server -- created only when the server is started
	 */
	public ServerFavicon getFavicon() {
		return favicon;
	}
	/**
	 * 
	 * @param favicon : new favicon object 
	 */
	public void setFavicon(ServerFavicon favicon) {
		this.favicon = favicon;
	}
	/**
	 * the maximum number of connections waiting in the queue maintained by the operating system
	 * @return the backlog value
	 */
	public int getBacklog() {
		return backlog;
	}
	/**
	 * sets the number of connections that can be queued up if the server socket is busy 
	 * Not to be kept a number greater than 100 - as it is a OS defined queue
	 * @param backlog the number of connections to be stored by the operating system
	 * @return true if the value passed was a valid number, else false - and backlog is set to default value of 50
	 */
	public boolean setBacklog(String backlog) {
		try{
			this.backlog = Integer.parseInt(backlog) ;
			if(this.backlog<0){
				this.backlog = 50 ;
				log.info("Backlog value cant be negative setting to default value: " + this.backlog);
				return false ;
			}
		}catch (NumberFormatException nfe){
			this.backlog = 50 ;
			log.info("Wrong value given. Setting to default value: " + this.backlog);
			return false ;
		}
		return true ;
	}

	/**
	 * The root directory that is served by the server if it exists (gets instantiated only when the server starts running)
	 * maybe different from the root directory supplied by the config file
	 * @return File object of the root directory
	 */
	public File getRootDir() {
		return rootDir;
	}
	/**
	 * sets the root directory to the File object supplied and if the directory cannot be executed/read upon the default root("DefaultHome") is served
	 * @param rootDir the File object that is to be served by the http web server
	 *  
	 */
	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}
	/**
	 * gets the upload directory name
	 * @return the valid upload directory name where the files will get stored upon upload
	 */
	public String getUploadPath() {
		return uploadPath;
	}
	/**
	 * Checks the validity of the supplied upload directory and initializes the upload directory variable to default upload path is the suplied 
	 * value is not a directory
	 * @param uploadPath the new upload path to set 
	 * @return true if the directory is valid, else false and sets it to defaultUpload as default upload folder
	 */
	public boolean setUploadPath(String uploadPath) {
		uploadPath = uploadPath.replace( "/", File.separator) ;
		uploadPath = uploadPath.replace("\\", File.separator) ;
		
		File checkUpload = new File(uploadPath) ;
		if(!checkUpload.isDirectory() || !checkUpload.canExecute() ){
			this.uploadPath = "DefaultUpload" ;
			log.error("the upload path provided cant be used as it is not a valid directory or there are no permissions to write in this directory "
					+ "Setting default upload directory");
			return false ;
		}
		if(uploadPath.endsWith(File.separator) ){
			this.uploadPath = uploadPath.substring(0, uploadPath.length()-1) ; // not taking the last '/' or '\'
		}else {
			this.uploadPath = uploadPath ;
		}
		return true ;
	}
	/**
	 * tests whether the server is running or not
	 * @return true if the server is accepting connections
	 */
	public Boolean getServerAccepts() {
		return serverAccepts;
	}
	/**
	 * Sets the server to an accepting state
	 * @param serverAccepts true if the server is to start accepting incoming connections
	 */
	public void setServerAccepts(boolean serverAccepts) {
		this.serverAccepts = serverAccepts;
	}
	/**
	 * the port to which the server is bound to 
	 * @return port value
	 */
	public int getPort() {
		return port;
	}
	/**
	 * sets the port to which the server is to be bound to
	 * @param strport the new port value
	 * @return true if the port number is valid to be a port else false  -- port checking/binding is not done here
	 */
	public boolean setPort(String strPort) {
		try{
			this.port = Integer.parseInt(strPort) ;
			if(this.port < 1024 && this.port>65536){
				this.port = 6666 ;
				return false ;
			}
		}catch (NumberFormatException nfe){
			log.info("Port number supplied is not in a correct format");
			this.port = 6666 ; 						// default port number
			return false ;
		}
		return true ;
	}
	/**
	 * the number of threads that will be always running at the backend
	 * @return the number of core threads
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}
	/**
	 * All changes to pool size must be done before starting the server
	 * @param corePoolSize the new pool size
	 * @return true if the value provided is a valid one else pool size is set to default - 3 and false is returned
	 */
	public boolean setCorePoolSize(String corePoolSize) {
		try {
			this.corePoolSize = Integer.parseInt(corePoolSize );
			if(this.corePoolSize < 0){
				this.corePoolSize = 3 ;
				log.info("Negative value of pool size not allowed, setting to default value "+this.corePoolSize);
				return false ;
			}
		}catch (NumberFormatException nfe ){
			this.corePoolSize = 3 ;					// default number for the core pool size
			log.info("Jumbled value of core pool size, setting to default value "+ this.corePoolSize);
			return false ;
		}
		return true ;
	}
	/**
	 * The maximum number of threads the backend can start
	 * @return number of maximum poosible/allowed threads
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	/**
	 * All changes to pool size must be done before starting the server
	 * @param maxPoolSize the new pool size
	 * @return true if the value provided is a valid one else pool size is set to default - 50 and false is returned
	 */
	public boolean setMaxPoolSize(String maxPoolSize) {
		try {
			this.maxPoolSize = Integer.parseInt(maxPoolSize);
			if(this.maxPoolSize<0){
				this.maxPoolSize = 50 ;
				log.info("Max pool size cannot be negative, setting to default value instead "+this.maxPoolSize);
				return false ;
			}
		}catch (NumberFormatException nfe ){
			this.maxPoolSize = 50 ;
			log.info("Max pool size is jumbled, setting to default value instead "+this.maxPoolSize);
			return false ;
		}
		return true ;
	}
		
	/**
	 * The maximum time a thread is allowed to wait or sit idle
	 * @return time in milliseconds
	 */
	public long getKeepAliveTime() {
		return keepAliveTime;
	}
	/**
	 * All changes to keep alive time must be done before starting the server
	 * @param keepAliveTime new keep alive time in milliseconds
	 * @return true if the value provided is a valid one else pool size is set to default - 5000 and false is returned
	 */
	public boolean setKeepAliveTime(String keepAliveTime) {
		try{
			this.keepAliveTime = Long.parseLong(keepAliveTime);
			if(this.keepAliveTime<0){
				this.keepAliveTime = 5000 ;
				log.info("keep alive time cannot be negative, setting to default value " + this.keepAliveTime);
				return false ;
			}
		}catch (NumberFormatException nfe ){
			this.keepAliveTime = 5000 ;
			log.info("Keep alive time is jumbled, setting to default value" + this.keepAliveTime);
			return false ;
		}
		return true ;
	}
	/**
	 * Sets the homepath and replaces the OS specific path separators with OS agnostic path separators
	 * 
	 * @param homePath the path of the home directory to be served
	 * @return true if the value was rightly set to what was provided, else false and default value loaded
	 */
	public boolean setHomePath(String homePath) {
		homePath = homePath.replace( "/", File.separator) ;
		homePath = homePath.replace("\\", File.separator) ;
		if(homePath.endsWith(File.separator) ){
			this.homePath = homePath.substring(0, homePath.length()-1) ;
		}else {
			this.homePath = homePath ;
		}
		rootDir = new File(homePath) ;
		try{
			if(!rootDir.isDirectory() || !rootDir.exists()){
				log.info("Provided Home Directory" + homePath +" is not a directory, changing default to defaultHome");
				this.homePath =  "DefaultHome" ;
				rootDir = new File(this.homePath) ;				// default home
				return false ;
			}
		}catch (SecurityException se){
			this.homePath = "DefaultHome" ;
			rootDir = new File(this.homePath) ;
			log.error("homeDirectory does not have read permissions or is not a directory, setting to default file");
			return false ;
		}		
		return true ;
	}
	/**
	 * Get the home directory path
	 * 
	 * @return home directory separated by OS specific delimiter ('\' or '/') 
	 */
	public String getHomePath(){
		return  homePath ;
	}
	/**
	 * gets the maximum length in number of bytes that can be allowed in the headers of a request
	 * @return the maximum length of request that can be received by the server ;
	 */
	public int getReqLength() {
		return reqLength;
	}
	/**
	 * sets the maximum length in number of bytes that can be allowed in the headers of a request
	 * @param reqLength the maximum allowed length of a request
	 * @return true if the value provided is a valid one else pool size is set to default - 1024 and false is returned
	 */
	public boolean setReqLength(String reqLength) {
		try {
			this.reqLength = Integer.parseInt(reqLength) ;
			if(this.reqLength<0){
				this.reqLength = 1024 ;
				log.info("Negative value not allowed for req length - setting default value "+ this.reqLength);
				return false ;
			}
		}catch(NumberFormatException nfe){
			this.reqLength = 1024 ;
			log.info("Garbled up value of req length - setting default value "+ this.reqLength);
			return false ;
		}
		return true ;
	}
/**
 * 	Makes a server object with values as specified
 * 
 * @param port : port number to bind the server to  
 * @param corePoolSize : the minimum number of threads to be always kept active
 * @param homePath : the home directory to be served 
 * @param maxPoolSize : the maximum number of threads that can be allowed to run
 * @param keepAliveTime : the keep alive time for every thread
 * @param backlog : the backlog number of connections that can be queued up on the server socket
 * @param faviconName : the name of the favicon associated with this server
 * @param uploadPath : The directory where all uploaded files will reside in
 */
	public Server( String homePath, int port, int corePoolSize , int maxPoolSize, long keepAliveTime, int backlog, int maxRequestLength, String faviconName, String uploadPath ) {
		this() ;														// For further scalability and also registering the basic connection manager and graceful exit 
		setHomePath(homePath ); 
		this.port = port;
		this.corePoolSize = corePoolSize ;
		this.maxPoolSize = maxPoolSize ;
		this.keepAliveTime = keepAliveTime ;
		this.backlog = backlog ;
		this.reqLength = maxRequestLength ;
		setUploadPath(uploadPath);
		favicon.setFileName(faviconName); ;
 	}
	/**
	 * Makes a server object
	 * @param port the port number to bind to
	 * @param uploadDir the directory where uploaded files will reside
	 */
	public Server (int port, String uploadDir){
		this();
		this.port = port ;
		setUploadPath(uploadDir);
		
	}
	/**
	 * Makes a server object
	 * @param homePath : the home directory to be served
	 * @param port : value of port number to bind the server to
	 * @param uploadDir :  the directory where uploaded files will reside 
	 */
	public Server( String homePath, int port, String uploadDir) {
		this() ;													// For further scalability and also registering the basic connection manager and graceful exit
		setHomePath(homePath );
		this.port = port ;
		setUploadPath(uploadDir);
	}
	/**
	 * Makes a server object
	 * @param homePath : the home directory to be served
	 * @param port : value of port number to bind the server to  
	 */
	public Server( String homePath, int port) {
		this() ;													// For further scalability and also registering the basic connection manager and graceful exit
		setHomePath(homePath );
		this.port = port ;
	}
	/**
	 * Makes a server object with default values of home directory, core pool size and max pool size and keep alive time 
	 * @param port : : value of port number to bind the server to
	 */
	public Server(int port) {
		this() ;													// For further scalability and also registering the basic connection manager and graceful exit
		this.port = port ;
	}
	/**
	 * Makes a server object with default values of port(6666), home directory = "DefaultHome", core pool size(3) and max pool size(50) 
	 * and keep alive time (5000 milliseconds).
	 */
	public Server() {
		homePath = "DefaultHome" ; 
		port = 6666;
		corePoolSize = 3 ;
		maxPoolSize = 50 ;
		keepAliveTime = 5000 ;
		backlog = 50 ;
		reqLength = 1024 ; 			// default max
		favicon = new ServerFavicon(this, "DefaultFavicon.ico") ;		// default name of the favicon file
		uploadPath = homePath + File.separator + "DefaultUpload" ;
		// -- basic things needed by the server object -- 
		//connections = new ConnectionManager(this) ;
		GracefulExitHook.addServer(this);				// adding to the list of servers that will be exited when shutdown hook runs
	}
	/**
	 * Creates a static HTML page in the home directory to help upload files to the server's upload directory using the browser
	 */
	public void createPostPortal(){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(getHomePath()+File.separator + "post.html", "UTF-8");
			writer.println("<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title> Welcome to the Post/Upload File utility</title> </head> <body>");
			//writer.println("<form method=\"post\" enctype=\"multipart/form-data\" action=\"http://localhost:"+ getPort() + "/\"> " ) ;
			writer.println("<form method=\"post\" enctype=\"multipart/form-data\" action=\"/\" \"> " ) ;
			
			writer.println("Choose the file1 to upload:") ;
			writer.println( "<input type=\"file\" name=\"fileID\" /><br />") ;
			writer.println("<input type=\"submit\" value=\"SEND\" />   </form> </body> </html>");
			writer.close();		
			log.info("Post portal prepared successfully at the home path - " + getHomePath() + " with port number config "+ getPort());
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			log.error("Post Portal could not be made");
		}
	}
	
	/**
	 * 
	 * Method binds to the specified port number at the localhost serves the directory provided in the homeDir and initiates 
	 * the backend with the provided 'numThreads' number of threads with the server socket with the timeout value as specified
	 */
	public void startServer() {
		Runtime.getRuntime().addShutdownHook(new GracefulExitHook());
		this.setServerAccepts(true);
		if(rootDir==null)	this.setRootDir( new File(this.getHomePath()) );
		favicon.loadFavicon(); 
		serverSocket = null ;
		try{
			serverSocket = new ServerSocket(port, backlog) ;
			log.info("Socked bound to specified port and has started accepting connections") ;
		}catch (IOException io){
			try {
				serverSocket = new ServerSocket(0, backlog);
			} catch (IOException e) {
				System.out.println("Could not start server on system supplied free port -- exiting the program");
				log.error("Could not start server on system supplied free port -- exiting the program");
				//e.printStackTrace();
			}
			port = serverSocket.getLocalPort() ;
			System.out.println("Server could not be started at the specified port -- instead running it on free port number - " + serverSocket.getLocalPort());
			log.error("Server could not be started at the specified port -- instead running it on free port number - " + serverSocket.getLocalPort());
		}
		createPostPortal() ;

		log.info("Server Started with values - " +
				" home directory - " + getHomePath() +
				" Port - " + getPort() +
				" core Pool Size - " + getCorePoolSize() +
				" max pool size - " + getMaxPoolSize() + 
				" Socket Backlog - " + getBacklog() +
				" Favicon File - " + favicon.getFileName() +
				" Keep alive time - " + getKeepAliveTime() +
				" Upload Path " + getUploadPath() ) ;
			
			//new Thread( new SpawnThreads(connections) ).start();
			engine = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, 
					TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()) ;
			int ct = 0; 
			
			while(serverAccepts){
				//System.out.println("+++++++++++++++");
				try{
					Socket incoming = serverSocket.accept() ;
					++ct;
					System.out.println("Request received count - " + ct );
					engine.execute(new ServerEngine(this, incoming, 0));
					//connections.addRequest(incoming, 0); 				// adding the incoming socket and the use count of this socket
				}catch (SecurityException se){
					log.error("Security manager not allowing socket coonections") ;
					//se.printStackTrace() ;
				}catch (IOException io) {
					log.info("Server Socket closed") ;
					//io.printStackTrace() ;
				}
			}
			
	}
	
	/**
	 * Stops the server threads and the removes the connections from the connection pool
	 * 
	 */
	public void stopServer(){
		log.info("Command to stop server running on port - "+ port + " - stop accepting new connections" );
		setServerAccepts(false);			// stop accepting new connections - disable socket acceptance
		log.info("shutting down " + engine.getTaskCount() +" tasks that are scheduled to run on server" );
		engine.shutdown(); 
		try {
			log.info("waiting for 10s for server to finish all connections");
			engine.awaitTermination(10, TimeUnit.SECONDS) ;
		} catch (InterruptedException e1) {
			log.error("Server threads interrupted before graceful ending - " + e1.getMessage() );
		//	e1.printStackTrace();
		}
		//connections.shutDownConnections();
		if(serverSocket!=null){		// now close the server socket after serving the last connections gracefully
			try {
				serverSocket.close() ;
			} catch (IOException e) {
				log.error("Unable to close server") ;
				e.printStackTrace();
			}
		}
		org.apache.log4j.LogManager.shutdown();
	}
}
