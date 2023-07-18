/**
 * 
 */
package org.ajax4jsf.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

/**
 * @author asmirnov
 * 
 */
public class ComponentsLoaderImpl implements Transformer, ComponentsLoader {

    private volatile Map classes;

    private ClassLoader loader;

    public ComponentsLoaderImpl() {
	classes = Collections.synchronizedMap(LazyMap.decorate(new HashMap(),
		this));
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.ajax4jsf.portlet.application.ComponentsLoader#createComponent(java.lang.String)
         */
    public UIComponent createComponent(String type) {
	// Classes is a lazy Map, new object will be create on the fly.
	Class componentClass = (Class) classes.get(type);
	try {
	    return (UIComponent) componentClass.newInstance();
	} catch (InstantiationException e) {
	    throw new FacesException(
		    "Error on create new instance of the component with class "
			    + type, e);
	} catch (IllegalAccessException e) {
	    throw new FacesException(
		    "IllegalAccess on attempt to create new instance of the component with class "
			    + type, e);
	}
    }

    public Object transform(Object input) {
	if (null == input) {
	    throw new NullPointerException(
		    "Name for a UIComponent class to restore is null");
	}
	ClassLoader loader = getClassLoader();
	Class componentClass = null;
	try {
	    componentClass = loader.loadClass(input.toString());
	} catch (ClassNotFoundException e) {
	    throw new FacesException("Can't load class " + input.toString(), e);
	}
	return componentClass;
    }

    /**
     * lazy create ClassLoader instance.
         * @return
         */
    protected synchronized ClassLoader getClassLoader() {
	if (loader == null) {
	    loader = Thread.currentThread().getContextClassLoader();
	    if (loader == null) {
		loader = this.getClass().getClassLoader();
	    }

	}
	return loader;
    }
}
