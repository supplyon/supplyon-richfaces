package org.ajax4jsf.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.ajax4jsf.Filter;
import org.ajax4jsf.exception.FileUploadException;

/**
 * Request wrapper for supporting multipart requests, used for file uploading.
 * 
 * @author Shane Bryzak
 */
public class MultipartRequest extends HttpServletRequestWrapper {
    private static final String PARAM_NAME = "name";
    private static final String PARAM_FILENAME = "filename";
    private static final String PARAM_CONTENT_TYPE = "Content-Type";

    private static final int BUFFER_SIZE = 2048;
    private static final int CHUNK_SIZE = 512;

    private boolean createTempFiles;
    
    private String uid;

    private String encoding = null;

    private Integer contentLength = 0;

    private int read = 0;
    
    private Map<String, Param> parameters = null;
    
    private Map<String, Object> percentMap = null;

    private enum ReadState {
	BOUNDARY, HEADERS, DATA
    }

    private static final byte CR = 0x0d;
    private static final byte LF = 0x0a;
    private static final byte[] CR_LF = { CR, LF };

    private abstract class Param {
	private String name;

	public Param(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public abstract void appendData(byte[] data, int start, int length)
		throws IOException;

    }

    private class ValueParam extends Param {
	private Object value = null;
	private ByteArrayOutputStream buf = new ByteArrayOutputStream();

	public ValueParam(String name) {
	    super(name);
	}

	@Override
	public void appendData(byte[] data, int start, int length)
		throws IOException {
	    // read += length;
	    buf.write(data, start, length);
	}

	public void complete() throws UnsupportedEncodingException {
	    String val = encoding == null ? new String(buf.toByteArray())
		    : new String(buf.toByteArray(), encoding);
	    if (value == null) {
		value = val;
	    } else {
		if (!(value instanceof List)) {
		    List<String> v = new ArrayList<String>();
		    v.add((String) value);
		    value = v;
		}

		((List) value).add(val);
	    }
	    buf.reset();
	}

	public Object getValue() {
	    return value;
	}
    }

    private class FileParam extends Param {
	private String filename;
	private String contentType;
	private int fileSize;

	private ByteArrayOutputStream bOut = null;
	private FileOutputStream fOut = null;
	private File tempFile = null;
	
	public FileParam(String name) {
	    super(name);
	}
	
	public Object getFile() {
			if (null != tempFile) {
				if (fOut != null) {
					try {
						fOut.close();
					} catch (IOException ex) {
					}
					fOut = null;
				}
				return tempFile;
			} else if (null != bOut) {
				return bOut.toByteArray();
			}
			return null;
	}

	public String getFilename() {
	    return filename;
	}

	public void setFilename(String filename) {
	    this.filename = filename;
	}

	public String getContentType() {
	    return contentType;
	}

	public void setContentType(String contentType) {
	    this.contentType = contentType;
	}

	public int getFileSize() {
	    return fileSize;
	}

	public File createTempFile() {
	    try {

		tempFile = File.createTempFile(new UID().toString().replace(
			":", "-"), ".upload");
		//tempFile.deleteOnExit();
		fOut = new FileOutputStream(tempFile);
	    } catch (IOException ex) {
		throw new FileUploadException("Could not create temporary file");
	    }
	    return tempFile;
	}
	
	public void deleteFile() {
			try {
				if (fOut != null) {
					fOut.close();
					if (tempFile != null) {
						tempFile.delete();
					}
				}
			} catch (Exception e) {
				throw new FileUploadException("Could not delete temporary file");
			}
		}

	@Override
	public void appendData(byte[] data, int start, int length)
		throws IOException {
	    // read += length;
	    if (fOut != null) {
		fOut.write(data, start, length);
		fOut.flush();
	    } else {
		if (bOut == null)
		    bOut = new ByteArrayOutputStream();
		bOut.write(data, start, length);
	    }

	    fileSize += length;
	}

	public byte[] getData() {
	    if (fOut != null) {
		try {
		    fOut.close();
		} catch (IOException ex) {
		}
		fOut = null;
	    }

	    if (bOut != null) {
		return bOut.toByteArray();
	    } else if (tempFile != null) {
		if (tempFile.exists()) {
		    try {
			FileInputStream fIn = new FileInputStream(tempFile);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			byte[] buf = new byte[512];
			int read = fIn.read(buf);
			while (read != -1) {
			    bOut.write(buf, 0, read);
			    read = fIn.read(buf);
			}
			bOut.flush();

			fIn.close();
			tempFile.delete();
			return bOut.toByteArray();
		    } catch (IOException ex) { /* too bad? */
		    }
		}
	    }

	    return null;
	}

	public InputStream getInputStream() {
	    if (fOut != null) {
		try {
		    fOut.close();
		} catch (IOException ex) {
		}
		fOut = null;
	    }

	    if (bOut != null) {
		return new ByteArrayInputStream(bOut.toByteArray());
	    } else if (tempFile != null) {
		try {
		    return new FileInputStream(tempFile) {
			@Override
			public void close() throws IOException {
			    super.close();
			    tempFile.delete();
			}
		    };
		} catch (FileNotFoundException ex) {
		}
	    }

	    return null;
	}
    }

    private HttpServletRequest request;
    
    private boolean shouldStop = false;

    public MultipartRequest(HttpServletRequest request,
	    boolean createTempFiles, int maxRequestSize, String uid) {
	super(request);
	this.request = request;
	this.createTempFiles = createTempFiles;
	this.uid = uid;

	String contentLength = request.getHeader("Content-Length");
	this.contentLength = Integer.parseInt(contentLength);
	if (contentLength != null && maxRequestSize > 0
		&& this.contentLength > maxRequestSize) {
	    //TODO : we should make decision if can generate exception in this place
	    //throw new FileUploadException(
		//    "Multipart request is larger than allowed size");
	}
    }

    private String decodeFileName(String name) {
    	String fileName = null;
		try {
			StringBuffer buffer = new StringBuffer();
			String[] codes = name.split(";");
			if (codes != null) {
				for (String code : codes) {
					if (code.startsWith("&")) {
						String sCode = code.replaceAll("[&#]*", "");
						Integer iCode = Integer.parseInt(sCode);
						buffer.append(Character.toChars(iCode));
					} else {
						buffer.append(code);
					}
				}
				fileName = buffer.toString();
			}
		} catch (Exception e) {
			fileName = name;
		}
		return fileName;
	}
    
    public void cancel() {
    	if (parameters != null) {
    		Iterator<Param> it = parameters.values().iterator();
    		while (it.hasNext()) {
    			Param p = it.next();
    			if (p instanceof FileParam) {
    				((FileParam)p).deleteFile();
    			}
    		}
    	}
    }

    public boolean parseRequest() {
    	byte[] boundaryMarker = getBoundaryMarker(request.getContentType());
    	if (boundaryMarker == null) {
    		throw new FileUploadException("The request was rejected because "
    				+ "no multipart boundary was found");
    	}

    	encoding = request.getCharacterEncoding();

    	parameters = new HashMap<String, Param>();
    	File file = null; 
    	this.percentMap = getProgressData();

    	try {
    		byte[] buffer = new byte[BUFFER_SIZE];
    		Map<String, String> headers = new HashMap<String, String>();

    		ReadState readState = ReadState.BOUNDARY;

    		InputStream input = request.getInputStream();
    		if (!shouldStop) {

    			int read = input.read(buffer);
    			int pos = 0;

    			Param p = null;

    			while (read != -1) {
    				for (int i = 0; i < read; i++) {
    					switch (readState) {
    					case BOUNDARY: {
    						if (checkSequence(buffer, i, boundaryMarker)
    								&& checkSequence(buffer, i + 2, CR_LF)) {
    							readState = ReadState.HEADERS;
    							i += 2;
    							pos = i + 1;

    						}
    						break;
    					}
    					case HEADERS: {
    						if (checkSequence(buffer, i, CR_LF)) {
    							String param = (encoding == null) ? new String(
    									buffer, pos, i - pos - 1) : new String(
    											buffer, pos, i - pos - 1, encoding);
    									parseParams(param, "; ", headers);

    									if (checkSequence(buffer, i + CR_LF.length, CR_LF)) {
    										readState = ReadState.DATA;
    										i += CR_LF.length;
    										pos = i + 1;

    										String paramName = headers.get(PARAM_NAME);
    										if (paramName != null) {
    											if (headers.containsKey(PARAM_FILENAME)) {
    												FileParam fp = new FileParam(paramName);
    												if (createTempFiles)
    												file = fp.createTempFile();
    												fp.setContentType(headers
    														.get(PARAM_CONTENT_TYPE));
    												fp.setFilename(decodeFileName(headers
    														.get(PARAM_FILENAME)));
    												p = fp;
    											} else {
    												if (parameters.containsKey(paramName)) {
    													p = parameters.get(paramName);
    												} else {
    													p = new ValueParam(paramName);
    												}
    											}

    											if (!parameters.containsKey(paramName)) {
    												parameters.put(paramName, p);
    											}
    										}

    										headers.clear();
    									} else {
    										pos = i + 1;
    									}
    						}
    						break;
    					}
    					case DATA: {
    						// If we've encountered another boundary...
    						if (checkSequence(buffer, i - boundaryMarker.length
    								- CR_LF.length, CR_LF)
    								&& checkSequence(buffer, i, boundaryMarker)) {
    							// Write any data before the boundary (that hasn't
    							// already been written) to the param
    							if (pos < i - boundaryMarker.length - CR_LF.length
    									- 1) {
    								p.appendData(buffer, pos, i - pos
    										- boundaryMarker.length - CR_LF.length
    										- 1);
    							}

    							if (p instanceof ValueParam)
    								((ValueParam) p).complete();

    							if (checkSequence(buffer, i + CR_LF.length, CR_LF)) {
    								i += CR_LF.length;
    								pos = i + 1;
    							} else {
    								pos = i;
    							}

    							readState = ReadState.HEADERS;
    						}
    						// Otherwise write whatever data we have to the param
    						else if (i > (pos + boundaryMarker.length + CHUNK_SIZE + CR_LF.length)) {
    							p.appendData(buffer, pos, CHUNK_SIZE);
    							pos += CHUNK_SIZE;

    						}
    						break;
    					}
    					}
    				}

    				if (!shouldStop) {
    					if (pos < read) {
    						// move the bytes that weren't read to the start of the
    						// buffer
    						int bytesNotRead = read - pos;
    						System.arraycopy(buffer, pos, buffer, 0, bytesNotRead);
    						read = input.read(buffer, bytesNotRead, buffer.length
    								- bytesNotRead);
    						read += bytesNotRead;
    					} else {
    						read = input.read(buffer);
    					}
    					this.read += pos;
    					pos = 0;
    					fillProgressInfo();
    				} else {
    					cancel();
    					return false;
    				}
    			}
    			
    			return true;
    		} else {
    		    cancel();
    			return false;
    		}
    	} catch (IOException ex) {
    		throw new FileUploadException("IO Error parsing multipart request",
    				ex);
    	}
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getProgressData () {
	percentMap  = (Map<String, Object>)getSession().getAttribute(Filter.PERCENT_BEAN_NAME);
	return percentMap;
    }
    
    private void fillProgressInfo () {
	Double percent = (Double) (100.0 * this.read / this.contentLength);
	percentMap.put(uid, percent);
	//this.percent = percent;
    }

    private byte[] getBoundaryMarker(String contentType) {
	Map<String, String> params = parseParams(contentType, ";");
	String boundaryStr = (String) params.get("boundary");

	if (boundaryStr == null)
	    return null;

	try {
	    return boundaryStr.getBytes("ISO-8859-1");
	} catch (UnsupportedEncodingException e) {
	    return boundaryStr.getBytes();
	}
    }

    /**
     * Checks if a specified sequence of bytes ends at a specific position
     * within a byte array.
     * 
     * @param data
     * @param pos
     * @param seq
     * @return boolean indicating if the sequence was found at the specified
     *         position
     */
    private boolean checkSequence(byte[] data, int pos, byte[] seq) {
	if (pos - seq.length < -1 || pos >= data.length)
	    return false;

	for (int i = 0; i < seq.length; i++) {
	    if (data[(pos - seq.length) + i + 1] != seq[i])
		return false;
	}

	return true;
    }

    private static final Pattern PARAM_VALUE_PATTERN = Pattern
	    .compile("^\\s*([^\\s=]+)\\s*[=:]\\s*(.+)\\s*$");
    
    private static final Pattern FILE_NAME_PATTERN = Pattern
    		.compile(".*filename=\"(.*)\"");

    private Map<String, String> parseParams(String paramStr, String separator) {
	Map<String, String> paramMap = new HashMap<String, String>();
	parseParams(paramStr, separator, paramMap);
	return paramMap;
    }

    private void parseParams(String paramStr, String separator, Map<String, String> paramMap) {
	String[] parts = paramStr.split(separator);

	for (String part : parts) {
	    Matcher m = PARAM_VALUE_PATTERN.matcher(part);
	    if (m.matches()) {
		String key = m.group(1);
		String value = m.group(2);

		// Strip double quotes
		if (value.startsWith("\"") && value.endsWith("\""))
		    value = value.substring(1, value.length() - 1);
		if (!"filename".equals(key)) {
		    paramMap.put(key, value);
		}else {
		    paramMap.put(key, parseFileName(paramStr));
		}
	    }
	}
    }
    
    private String parseFileName(String parseStr) {
	Matcher m = FILE_NAME_PATTERN.matcher(parseStr);
	if (m.matches()) {
		String name = m.group(1);
		if (name.startsWith("&")) {
		    return decodeFileName(name);
		} else{
		    return name;
		}
	}
	return null;
    }

    private Param getParam(String name) {
	if (parameters == null)
	    parseRequest();
	return parameters.get(name);
    }
    
           
    public Integer getSize() {
	return contentLength;
    }

    @Override
    public Enumeration getParameterNames() {
	if (parameters == null)
	    parseRequest();

	return Collections.enumeration(parameters.keySet());
    }

    public byte[] getFileBytes(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getData() : null;
    }

    public InputStream getFileInputStream(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getInputStream() : null;
    }

    public String getFileContentType(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getContentType() : null;
    }
    
    public Object getFile(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getFile() : null;
    }
 
    public String getFileName(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getFilename() : null;
    }

    public int getFileSize(String name) {
	Param p = getParam(name);
	return (p != null && p instanceof FileParam) ? ((FileParam) p)
		.getFileSize() : -1;
    }

    @Override
    public String getParameter(String name) {
	Param p = getParam(name);
	if (p != null && p instanceof ValueParam) {
	    ValueParam vp = (ValueParam) p;
	    if (vp.getValue() instanceof String)
		return (String) vp.getValue();
	} else if (p != null && p instanceof FileParam) {
	    return "---BINARY DATA---";
	} else {
	    return super.getParameter(name);
	}

	return null;
    }

    @Override
    public String[] getParameterValues(String name) {
	Param p = getParam(name);
	if (p != null && p instanceof ValueParam) {
	    ValueParam vp = (ValueParam) p;
	    if (vp.getValue() instanceof List) {
		List vals = (List) vp.getValue();
		String[] values = new String[vals.size()];
		vals.toArray(values);
		return values;
	    } else {
		return new String[] { (String) vp.getValue() };
	    }
	} else {
	    return super.getParameterValues(name);
	}
    }

    @Override
    public Map getParameterMap() {
	if (parameters == null)
	    parseRequest();

	Map<String, Object> params = new HashMap<String, Object>(super
		.getParameterMap());

	for (String name : parameters.keySet()) {
	    Param p = parameters.get(name);
	    if (p instanceof ValueParam) {
		ValueParam vp = (ValueParam) p;
		if (vp.getValue() instanceof String) {
		    params.put(name, vp.getValue());
		} else if (vp.getValue() instanceof List) {
		    params.put(name, getParameterValues(name));
		}
	    }
	}

	return params;
    }
    
    public void stop() {
    	shouldStop = true;
    }
}
