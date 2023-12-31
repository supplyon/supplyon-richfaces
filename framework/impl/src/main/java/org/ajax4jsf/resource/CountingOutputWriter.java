/*
 * CountingOutputWriter.java		Date created: 21.11.2007
 * Last modified by: $Author$
 * $Revision$	$Date$
 */

package org.ajax4jsf.resource;

import java.io.IOException;
import java.io.Writer;

/**
 * Class provides custom writer implementation with counting of bytes written
 * Is using for replacement of css component writer
 * @author Andrey Markavtsov
 */
public class CountingOutputWriter extends Writer {
	
	/** count of written bytes */
	private int written = 0;
	
	/** Size of char type */
	public static final int sizeOfChar = 1;
	
	/** Size of int type */
	public static final int sizeOfInt = 2;
	
	/** Buffer to store bytes written */
	private StringBuffer buffer; 
			
	/**
	 * Default constructor 
	 */
	public CountingOutputWriter() {
   	
		super();
		this.buffer = new StringBuffer();
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#close()
	 */
	public void close() throws IOException {
		;
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#flush()
	 */
	public void flush() throws IOException {
		;
	}

    /** Methods appends chars written to buffer
	 *  @param cbuf - chars to be written
	 *  @param off  - offset
	 *  @param len  - length of bytes 
	 */ 
	public void write(char[] cbuf, int off, int len) throws IOException {
		buffer.append(cbuf, off, len);
		written += len; // * sizeOfChar;
	}

	/** Methods appends chars written to buffer
	 *  @param cbuf - chars to be written
	 */ 
	public void write(char[] cbuf) throws IOException {
		buffer.append(cbuf);
		written += cbuf.length; // * sizeOfChar;
	}

    /** Methods appends int written to buffer
	 *  @param c - int to be written
	 */ 
	public void write(int c) throws IOException {
		buffer.append(Character.toChars(c));
		written += sizeOfInt;
	}

	 /** Methods appends string written to buffer
	 *  @param str  - string to be written
	 *  @param off  - offset
	 *  @param len  - length of bytes 
	 */ 
	public void write(String str, int off, int len) throws IOException {
		buffer.append(str, off, len);
		written += len;// * sizeOfChar;
	}

	/** Methods appends string written to buffer
	 *  @param str - string to be written
     */
	public void write(String str) throws IOException {
		buffer.append(str);
		written += str.length();// * sizeOfChar;
	}

	/** Methods gets written bytes count
	 *  @return written count of bytes 
	 */
	public int getWritten() {
		return written;
	}
	
	/** Methods gets content of written bytes
	 *  @return buffer  
	 */
	public StringBuffer getContent () {
		return buffer;
	}
}