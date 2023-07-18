/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.ajax4jsf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Abstraction context class for rendering resource ( image, script, style )
 * can be work in 2 variants - for simple ServletRequest and as JSF context encapsulation.
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:56:57 $
 *
 */
public abstract class ResourceContext  {
	
	private boolean cacheEnabled = false;
	private Object resourceData;
	// response headers
	/**
	 * Delegate to {@link javax.servlet.ServletResponse} setHeader
	 * @param name name of header
	 * @param value new value
	 */
	public abstract void setHeader(String name, String value);
	
	/**
	 * Delegate to {@link javax.servlet.ServletResponse} setHeader
	 * @param name name of header
	 * @param value new value
	 */
	public abstract void setIntHeader(String name, int value);

	/**
	 * Delegate to {@link javax.servlet.ServletResponse} setHeader
	 * @param name name of header
	 * @param value new value
	 */
	public abstract void setDateHeader(String name, long value);
	
	/**
	 * @return
	 * @throws IOException 
	 */
	public abstract OutputStream getOutputStream() throws IOException;
	
	/**
	 * @return
	 */
	public abstract String getQueryString();
	
	/**
	 * @return
	 */
	public abstract String getPathInfo();
	
	/**
	 * @return
	 */
	public abstract String getServletPath();
	
	/**
	 * Get session attribute for given name. session not created
	 * @param name attribute name
	 * @return value for attribute, or null.
	 */
	public abstract Object getSessionAttribute(String name);

	public abstract InputStream getResourceAsStream(String path);

	/**
	 * Get request parameter for given name.
	 * @param data_parameter
	 * @return
	 */
	public abstract String getRequestParameter(String data_parameter);

	/**
	 * @return Returns the cacheEnabled.
	 */
	public boolean isCacheEnabled() {
		return this.cacheEnabled;
	}

	/**
	 * @param cacheEnabled The cacheEnabled to set.
	 */
	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	/**
	 * get output writer for send response.
	 * @return
	 */
	public abstract PrintWriter getWriter() throws IOException;

	/**
	 * Setup response content type as {@see javax.servlet.ServletResponse#setContentType(java.lang.String)}
	 * @param contentType
	 */
	public abstract void setContentType(String contentType);
	
	public abstract String getInitParameter(String name);

	public Object getResourceData() {
		// TODO Auto-generated method stub
		return resourceData;
	}

	public void setResourceData(Object data) {
		resourceData = data;
		
	}
	
	/**
	 * Release any data used by this context.
	 * Close buffers used by cached context, release FacesContext, if exist.
	 */
	public void release() {
		
	}
	
}
