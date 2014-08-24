package LowLevelUtilities;
/**
 * A utility class that contains a string and the number of bytes that took to make this string
 * @author rohtalwa
 *
 */
public class MyLine {
	private String line ;
	private long bytes ;
	public MyLine(String line, long bytes){
		this.line = line ;
		this.bytes = bytes ;
	}
	/**
	 * 
	 * @return the string object
	 */
	public String getLine() {
		return line;
	}
	
	public void setLine(String line) {
		this.line = line;
	}
	/**
	 * 
	 * @return gets the number of bytes read or number bytes which constitute this string object
	 */
	public long getBytes() {
		return bytes;
	}
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	
}
