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

package org.ajax4jsf.resource.cached;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;

import org.ajax4jsf.Messages;
import org.ajax4jsf.resource.InternetResource;
import org.ajax4jsf.resource.ResourceBuilderImpl;
import org.ajax4jsf.resource.ResourceNotFoundException;
import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author shura
 * 
 */
public class CachedResourceBuilder extends ResourceBuilderImpl {

    private static final Log log = LogFactory
	    .getLog(CachedResourceBuilder.class);

    private static final int DEFAULT_CAPACITY = 10000;

    private long counter = 0;

    private DualLRUMap cache;

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#decrypt(byte[])
         */
    protected byte[] decrypt(byte[] data) {
	// dummy - data not send via internet.
	return data;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#encrypt(byte[])
         */
    protected byte[] encrypt(byte[] data) {
	// dummy - data not send via internet.
	return data;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#getResourceDataForKey(java.lang.String)
         */
    public Object getResourceDataForKey(String key) {
	ResourceBean bean = (ResourceBean) cache.get(key);
	if (null == bean) {
	    throw new ResourceNotFoundException("Resource for key " + key
		    + "not present in cache");
	}
	return bean.getData();
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#getResourceForKey(java.lang.String)
         */
    public InternetResource getResourceForKey(String key)
	    throws ResourceNotFoundException {
	ResourceBean bean = (ResourceBean) cache.get(key);
	if (null == bean) {
	    throw new ResourceNotFoundException("Resource for key " + key
		    + "not present in cache");
	}
	return super.getResourceForKey(bean.getKey());
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#getUri(org.ajax4jsf.resource.InternetResource,
         *      javax.faces.context.FacesContext, java.lang.Object)
         */
    public String getUri(InternetResource resource, FacesContext facesContext,
	    Object data) {
	ResourceBean bean;
	if (null == data) {
	    bean = new ResourceBean(resource.getKey());
	} else {
	    if (data instanceof byte[]) {
		// Special case for simple bytes array data.
		bean = new ResourceBytesDataBean(resource.getKey(),
			(byte[]) data);
	    } else {
		bean = new ResourceDataBean(resource.getKey(), data);
	    }
	}
	String key = (String) cache.getKey(bean);
	if (null == key) {
	    synchronized (this) {
		counter++;
		key = bean.hashCode() + "c" + counter;
	    }
	    cache.put(key, bean);
	} else {
	    // Refresh LRU
	    cache.get(key);
	}
	String resourceURL = getFacesResourceURL(facesContext, key);
	if (resource.isSessionAware()) {
	    resourceURL = facesContext.getExternalContext().encodeResourceURL(
		    resourceURL);
	}
	if (log.isDebugEnabled()) {
	    log.debug(Messages.getMessage(Messages.BUILD_RESOURCE_URI_INFO,
		    resource.getKey(), resourceURL));
	}
	return resourceURL;// context.getExternalContext().encodeResourceURL(resourceURL);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.resource.ResourceBuilderImpl#init(javax.servlet.ServletContext,
         *      java.lang.String)
         */
    public void init()
	    throws FacesException {
	super.init();
	// Create cache manager.
	Properties properties = getProperties("cache.properties");
	int capacity = DEFAULT_CAPACITY;
	String capacityString = properties.getProperty("cache.capacity");
	if (null != capacityString) {
	    try {
		capacity = Integer.parseInt(capacityString);
	    } catch (NumberFormatException e) {
		log.warn("Error parsing value of parameters cache capacity, use default value "+DEFAULT_CAPACITY, e);
	    }
	}
	cache = new DualLRUMap(capacity);
	counter = getStartTime() - 1158760000000L;
    }

    /**
         * Get properties file from classpath
         * 
         * @param name
         * @return
         */
    protected Properties getProperties(String name) {
	Properties properties = new Properties();
	InputStream props = URLToStreamHelper.urlToStreamSafe(CachedResourceBuilder.class
		.getResource(name));
	if (null != props) {
	    try {
		properties.load(props);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		log.warn(Messages.getMessage(Messages.READING_PROPERTIES_ERROR,
			name), e);
	    } finally {
		try {
		    props.close();
		} catch (IOException e) {
		    // Can be ignored
		}
	    }
	}
	return properties;

    }
}
