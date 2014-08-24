package LowLevelUtilities;
/*
package adobe.WebServer;

import java.io.InputStreamReader;

public class OptimizedRead {
	private int bufferLen;
	private int maxLineLength ;
	private char buffer[] ;
	char lineBuf[] ;

	public OptimizedRead( int bufferLen, int maxLineLength){
		this.bufferLen = bufferLen ;
		this.maxLineLength = maxLineLength ;
		buffer = new char[bufferLen + 1];
		lineBuf = new char[maxLineLength];
	}
	// http://www.kegel.com/java/wp-javaio.html
	// This example uses character arrays instead of Strings.
	// It doesn't use BufferedReader or BufferedFileReader, but does
	// the buffering by itself so that it can avoid creating too many
	// String objects.  For simplicity, it assumes that no line will be
	// longer than 128 characters.
	public char[] readNextLine(InputStreamReader ir){
		try {
			int nChars = 0;
			int nextChar = 0;
			int startChar = 0;
			boolean eol = false;
			int lineLength = 0;
			char c = 0;
			int n;
			int j;
			while (true) {
				if (nextChar >= nChars) {
					n = ir.read(buffer, 0, bufferLen);
					if (n == -1) {  // EOF
						break;
					}
					nChars = n;
					startChar = 0;
					nextChar = 0;
				}

				for (j=nextChar; j < nChars; j++) {
					c = buffer[j];
					if ((c == '\n') || (c == '\r')) {
						eol = true;
						break;
					}
				}
				nextChar = j;

				int len = nextChar - startChar;
				if (eol) {
					nextChar++;
					if ((lineLength + len) > maxLineLength) {
						// error
					} else {
						System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
					}
					lineLength += len;
					return lineBuf ;
					// 
					// Process line here
					//

					if (c == '\r') {
						if (nextChar >= nChars) {
							n = ir.read(buffer, 0, bufferLen);
							if (n != -1) {
								nextChar = 0;
								nChars = n;
							}
						}

						if ((nextChar < nChars) && (buffer[nextChar] == '\n'))
							nextChar++;
					}
					startChar = nextChar;
					lineLength = 0;
					continue;
				}

				if ((lineLength + len) > maxLineLength) {
					// error
				} else {
					System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
				}
				lineLength += len;
			}
			ir.close();
		} catch (Exception e) {
			System.out.println("exception: " + e);
		}
	}
}
*/