/*
 * UploadItem.java		Date created: 03.03.2008
 * Last modified by: $Author$
 * $Revision$	$Date$
 */

package org.richfaces.model;

import java.io.File;
import java.io.Serializable;

import javax.faces.FacesException;

/**
 * Class provides object holder for file uploaded.
 * Instance of this type will be returned by UploadEvent after appropriate listener called after uploading has been completed.    
 * @author "Andrey Markavtsov"
 *
 */
public class UploadItem implements Serializable{
    
    /**
	 * Serial id
	 */
	private static final long serialVersionUID = -111723029745124147L;

	/** Users file name */
    private String fileName;
    
    /** Content type */
    private String contentType;
    
    /** java.io.File instance */
    private File file;
    
    /** File byte content */
    private byte [] bytes;
    
    /**
     * Constructor for the UploadItem
     */
    public UploadItem(String fileName, String contentType, Object file) {
	this.fileName = fileName;
	this.contentType = contentType;
	if (null != file) {
	    if (file.getClass().isAssignableFrom(File.class)) {
		this.file = (File) file;
	    } else if (file.getClass().isAssignableFrom(byte[].class)) {
		this.bytes = (byte[]) file;
	    }
	}
    }
    
    /**
     * Return true if file is holding as java.io.File type.
     * If true getFile method should be invoked to get file uploaded.
     * In another case getData method should be invoked to get file's bytes.
     * @return
     */
    public boolean isTempFile () {
	return (null != file);
    }

    /**
     * This method should called only in case of TRUE value returned by {@link #isTempFile()} method.
     * Otherwise null value will be returned by this method. 
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * This method should called only in case of FALSE value returned by {@link #isTempFile()} method.
     * Otherwise null value will be returned by this method. 
     * @return the bytes
     * @throws Exception 
     */
    public byte[] getData() {
        return bytes;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
    
    
}
