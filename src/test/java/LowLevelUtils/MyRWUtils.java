package LowLevelUtils;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;


public class MyRWUtils {
	private int start ;
	private byte[] buffer ;
	private int bufferCapacity ;
	private InputStream in ;
	private String encoding ;
	/**
	 * Returns the input stream of this object
	 * @return the socket connections client side input stream
	 */
	public InputStream getInputStream(){
		return in ;
	}


	public MyRWUtils(InputStream in, String encoding){
		bufferCapacity = 4096 ;
		buffer = new byte[bufferCapacity] ;
		this.in = in ;
		this.encoding = encoding ;
	}
	/**
	 * Sends the post request to the output stream of the client that is connected to the Internet.
	 * @param file the complete file path to send
	 * @param out the client-server socket connections output socket
	 * @return true if the request was sent successfully else false
	 */
	public boolean sendPOSTRequest(String file, OutputStream out, String filePath){
		
		String dashes = "---------------------------" ;
		long boundary = System.currentTimeMillis() ;
		File fileToSend = new File(filePath) ;
		if(!fileToSend.exists() || !fileToSend.isFile() || !fileToSend.canRead()){
			return false ;
		}
		long contentLength = fileToSend.length() ;
		String firstLine = "--" + dashes + Long.toString(boundary) + "\r\n" ;
		String secondLine = "Content-type: " + URLConnection.getFileNameMap().getContentTypeFor(file) + " , filename=\"" + file + "\"\r\n" ;
		String thirdLine = "Content - i dont care!\r\n\r\n" ;
		String lastLine = "\r\n--" + dashes + boundary + "--\r\n" ;
		byte[] a=null, b=null , c=null, d=null;
		try {
			a = firstLine.getBytes(encoding);
			b = secondLine.getBytes(encoding) ;
			c = thirdLine.getBytes(encoding) ;
			d = lastLine.getBytes(encoding) ;
			contentLength += a.length + b.length + c.length + d.length ;
			String request = "POST / HTTP/1.1\r\n" +
					"Host: localhost:6666\r\n" +
					"Connection: keep-alive\r\n" +
					"Content-Type: multipart/form-data; boundary=" + dashes + Long.toString(boundary) + "\r\n"  +  	// need to add content length
					"Content-Length: " + contentLength + "\r\n\r\n" ;
			byte header[] = request.getBytes(encoding) ;
			out.write(header);	
			out.write(a);
			out.write(b);
			out.write(c);
			FileInputStream input = new FileInputStream(fileToSend); 			// reading the file from disk
			long count = 0;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {					// -1 is End of File
				out.write(buffer, 0, n);
				count += n;
			}
			input.close();
			out.write(d);
			out.flush(); 
			if(count != fileToSend.length() )	return false ;		// could not send complete file
		}catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return true ;
	}

	/**
	 * Reads a line from the input stream and converts using the encoding provided
	 * @return null if nothing is on the input stream, else the read string is returned INCLUDING the new line chars
	 */
	public String readLine(){
		start = -1;
		int a ;
		String s = null;
		try {
			while( ( a = in.read() ) != -1 ){
				++start ;
				if(start >= bufferCapacity){		// erroneus string
					s = new String(buffer, 0, start, encoding) ;
					//log.error("The buffer limit reached the string is being sent abruptly");
					break ;
				}
				buffer[start] = (byte)a ;
				if((char)a == '\n' ){
					++start ;
					s = new String(buffer, 0, start, encoding) ;
					break ;
				}
			}
			if(s==null && start!=-1){
				s = new String(buffer, 0, start+1, encoding) ;			// say we dont get \r or \n but the eof is reached we still need to send the string so formed
			}
		} catch (IOException e) {
			fail("Socket is broken cant read next byte from stream ");
			s = "Closed" ;
		}
		return s ;
	}
	/**
	 * Compares the data on the stream with the data of the file name specified  
	 * @param fileToCompare
	 * @return true if both files match fully else false
	 * @throws IOException 
	 */
	public boolean checkBytes(String fileToCompare, long dataInStream) throws IOException{
		//		FileInputStream trueInput = new FileInputStream(fileToCompare); 			// reading the file from disk
		//		long count = 0;
		//		int fileBuf = 0, socketBuf = 0;								// stores the amount of data received into the buffer
		//		byte[] fileBuffer = new byte[4096] ;							// 4kb buffer size
		//		while (-1 != (fileBuf = trueInput.read(fileBuffer))) {					// -1 is End of File
		//			
		//		}


		String rcvdFile = Long.toString(System.currentTimeMillis()) ;		// temp name of the file created
		//log.info("Temp file being created with name - " + rcvdFile);
		int realCapacity = bufferCapacity ;
		BufferedOutputStream writeToFile = null;
		long size = dataInStream ;
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
			fail("File not found or cannot be created");
			return false ;
		} catch (IOException e) {
			fail("Writing to file was interrupted unexpectedly");
			return false ;
		}
		bufferCapacity = realCapacity ;				// reinitializing the buffercapacity
		File tmp = new File(rcvdFile) ;
		boolean comp = isFileBinaryEqual(new File(fileToCompare), tmp) ;
		tmp.delete() ;
		return comp ;
	}
	/**
	 * Compare binary files. Both files must be files (not directories) and exist.
	 * 
	 * @param first  - first file
	 * @param second - second file
	 * @return boolean - true if files are binery equal
	 * @throws IOException - error in function
	 * @author Miro Halas
	 * Credits - http://www.java2s.com/Code/Java/File-Input-Output/Comparebinaryfiles.htm
	 */
	public boolean isFileBinaryEqual(File first, File second) throws IOException {
		boolean retval = false;
		if ((first.exists()) && (second.exists()) 
				&& (first.isFile()) && (second.isFile()))
		{
			if (first.getCanonicalPath().equals(second.getCanonicalPath()))
			{
				retval = true;
			}
			else
			{
				FileInputStream firstInput = null;
				FileInputStream secondInput = null;
				BufferedInputStream bufFirstInput = null;
				BufferedInputStream bufSecondInput = null;

				try
				{            
					firstInput = new FileInputStream(first); 
					secondInput = new FileInputStream(second);
					bufFirstInput = new BufferedInputStream(firstInput, 65536); 
					bufSecondInput = new BufferedInputStream(secondInput, 65536);

					int firstByte;
					int secondByte;

					while (true)
					{
						firstByte = bufFirstInput.read();
						secondByte = bufSecondInput.read();
						if (firstByte != secondByte)
						{
							break;
						}
						if ((firstByte < 0) && (secondByte < 0))
						{
							retval = true;
							break;
						}
					}
				}
				finally
				{
					try
					{
						if (bufFirstInput != null)
						{
							bufFirstInput.close();
						}
					}
					finally
					{
						if (bufSecondInput != null)
						{
							bufSecondInput.close();
						}
					}
				}
			}
		}
		return retval;
	}

}
