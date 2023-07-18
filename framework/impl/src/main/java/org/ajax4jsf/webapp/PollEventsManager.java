/**
 * 
 */
package org.ajax4jsf.webapp;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import javax.faces.FacesException;
import javax.servlet.ServletContext;

import org.ajax4jsf.cache.Cache;
import org.ajax4jsf.cache.CacheConfigurationLoader;
import org.ajax4jsf.cache.CacheException;
import org.ajax4jsf.cache.CacheFactory;
import org.ajax4jsf.cache.CacheLoader;
import org.ajax4jsf.cache.CacheManager;
import org.ajax4jsf.cache.ServletContextInitMap;

/**
 * @author asmirnov
 *
 */
public class PollEventsManager implements Serializable, CacheLoader, CacheConfigurationLoader {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6257285396790747665L;

	public static final String EVENTS_MANAGER_KEY=PollEventsManager.class.getName();

    private Cache cache;
    
    public void init(ServletContext servletContext) {
	    try {
		CacheManager cacheManager = CacheManager.getInstance();
		Map<String, String> env = new ServletContextInitMap(servletContext);
		CacheFactory cacheFactory = cacheManager.getCacheFactory(env);
		this.cache = cacheFactory.createCache(env, this, this);
		servletContext.setAttribute(EVENTS_MANAGER_KEY, this);
	} catch (CacheException e) {
		throw new FacesException(e.getMessage(), e);
	}

    }
    
    public PushEventsCounter getListener(String key){
	if(null == cache){
	    throw new FacesException("Poll events manager not initialized");	    
	}
	try {
	    return (PushEventsCounter) cache.get(key, null);
	} catch (CacheException e) {
	    throw new FacesException("error get push events listener for key "+key,e);
	}
    }

    public Object load(Object key, Object context) throws CacheException {
	// TODO Auto-generated method stub
	return new PushEventsCounter();
    }

    public Properties loadProperties(String name) {
	return new Properties();
    }
}
