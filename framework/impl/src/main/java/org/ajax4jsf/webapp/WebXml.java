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

package org.ajax4jsf.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.Messages;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.richfaces.VersionBean;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Parse at startup application web.xml and store servlet and filter mappings.
 * at runtime, used for convert resource key to uri, and vice versa.
 * 
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:58:59 $
 * 
 */
public class WebXml implements Serializable {

	public static final String CONTEXT_ATTRIBUTE = WebXml.class.getName();

	/**
	 * 
	 */
	private static final long serialVersionUID = -9042908418843695017L;

	private static final Log _log = LogFactory.getLog(WebXml.class);

	static final String WEB_XML = "/WEB-INF/web.xml";

	public static final String RESOURCE_URI_PREFIX = "a4j_"
			+ VersionBean.MAJOR_VERSION + "_" + VersionBean.MINOR_VERSION + "_"
			+ "1-SNAPSHOT"; //+ VersionBean.REVISION;

	static final String RESOURCE_URI_PREFIX_WITH_SLASH = "/"
			+ RESOURCE_URI_PREFIX;

	public static final String RESOURCE_URI_PREFIX_PARAM = "org.ajax4jsf.RESOURCE_URI_PREFIX";

	/**
	 * Prefix for Resourse-Ajax filter, in common must be same as for
	 * {@link javax.faces.webapp.FacesServlet}
	 */
	private String _facesFilterPrefix = null;

	/**
	 * Suffix for Resource-Ajax filter , in common must be same as for
	 * {@link javax.faces.webapp.FacesServlet}
	 */
	private String _facesFilterSuffix = null;

	private String _facesServletPrefix = null;

	private String _facesServletSuffix = null;

	private boolean _prefixMapping = false;

	private String _filterName;

	private Map<String, ServletBean> _servlets = new HashMap<String, ServletBean>();

	private Map<String, FilterBean> _filters = new HashMap<String, FilterBean>();

	/**
	 * Prefix for resources handled by Chameleon framework.
	 */
	private String _resourcePrefix = RESOURCE_URI_PREFIX_WITH_SLASH;

	public static WebXml getInstance() {
		return getInstance(FacesContext.getCurrentInstance());
	}
	
	public static WebXml getInstance(FacesContext context) {
		WebXml webXml = (WebXml) context.getExternalContext()
				.getApplicationMap().get(WebXml.CONTEXT_ATTRIBUTE);
		return webXml;
	}
	
	
	/**
	 * Parse application web.xml configuration and detect mapping for resources
	 * and logs.
	 * 
	 * @param context
	 * @param filterName
	 * @throws ServletException
	 */
	public void init(ServletContext context, String filterName)
			throws ServletException {
		InputStream webXml = context.getResourceAsStream(WEB_XML);
		if (null == webXml) {
			throw new ServletException(Messages.getMessage(
					Messages.GET_RESOURCE_AS_STREAM_ERROR, WEB_XML));
		}
		Digester dig = new Digester();
		dig.setDocumentLocator(new LocatorImpl());
		// Disable xml validations at all - web.xml already validated by
		// container
		dig.setValidating(false);
		dig.setEntityResolver(new EntityResolver() {
			// Dummi resolver - alvays do nothing
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}

		});
		dig.setNamespaceAware(false);
		// dig.setUseContextClassLoader(true);
		dig.setClassLoader(this.getClass().getClassLoader());
		// Parsing rules.
		// Servlets.
		String path = "web-app/servlet";
		dig.addObjectCreate(path, ServletBean.class);
		dig.addBeanPropertySetter(path + "/servlet-name", "servletName");
		dig.addBeanPropertySetter(path + "/servlet-class", "servletClass");
		dig.addBeanPropertySetter(path + "/display-name", "displayName");
		dig.addBeanPropertySetter(path + "/description");
		dig.addSetNext(path, "addServlet");
		// Filters
		path = "web-app/filter";
		dig.addObjectCreate(path, FilterBean.class);
		dig.addBeanPropertySetter(path + "/filter-name", "filterName");
		dig.addBeanPropertySetter(path + "/filter-class", "filterClass");
		dig.addBeanPropertySetter(path + "/display-name", "displayName");
		dig.addBeanPropertySetter(path + "/description");
		dig.addSetNext(path, "addFilter");
		// Servlet mappings
		path = "web-app/servlet-mapping";
		dig.addCallMethod(path, "addServletMapping", 2);
		dig.addCallParam(path + "/servlet-name", 0);
		dig.addCallParam(path + "/url-pattern", 1);
		// Filter mappings
		// TODO - parse dispatcher.
		path = "web-app/filter-mapping";
		dig.addCallMethod(path, "addFilterMapping", 3);
		dig.addCallParam(path + "/filter-name", 0);
		dig.addCallParam(path + "/url-pattern", 1);
		dig.addCallParam(path + "/servlet-name", 2);
		dig.push(this);
		try {
			dig.parse(webXml);
			this.setFilterName(filterName, context);
			this.findFacesServlet(context);
			// Store Instance to context attribute.
			context.setAttribute(CONTEXT_ATTRIBUTE, this);
		} catch (IOException e) {
			String message = Messages
					.getMessage(Messages.PARSING_WEB_XML_IO_ERROR);
			_log.error(message, e);
			throw new ServletException(message, e);
		} catch (SAXException e) {
			String message = Messages
					.getMessage(Messages.PARSING_WEB_XML_SAX_ERROR);
			_log.error(message, e);
			throw new ServletException(message, e);
		} finally {
			try {
				webXml.close();
			} catch (IOException e) {
				// this exception don't affect any aspects of work and can be
				// ignored.
			}
		}
	}

	private void findFacesServlet(ServletContext context) {
		for (Iterator<Entry<String, ServletBean>> servletsIterator = _servlets
				.entrySet().iterator(); servletsIterator.hasNext();) {
			Entry<String, ServletBean> servletEntry = servletsIterator.next();
			String servletClass = servletEntry.getValue().getServletClass();
			if("javax.faces.webapp.FacesServlet".equals(servletClass)){
				Mapping mapping = checkMapping(servletEntry.getValue().getMappings());
				if(null != mapping){
					this._facesServletPrefix = mapping.getPrefix();
					this._facesServletSuffix = mapping.getSuffix();
				}
			}
		}

	}

	public void addServlet(ServletBean bean) {
		String name = bean.getServletName();
		if (null != name) {
			_servlets.put(name, bean);
		}
	}

	public void addFilter(FilterBean bean) {
		String name = bean.getFilterName();
		if (null != name) {
			_filters.put(name, bean);
		}

	}

	public void addServletMapping(String servletName, String mapping) {
		ServletBean servletBean = (ServletBean) _servlets.get(servletName);
		if (null != servletBean) {
			(servletBean).addMapping(mapping);
		}
	}

	public void addFilterMapping(String filterName, String mapping,
			String servlet) {
		FilterBean filterBean = (FilterBean) _filters.get(filterName);
		if (null != filterBean) {
			if (null != mapping) {
				(filterBean).addMapping(mapping);
			}
			if (null != servlet) {
				(filterBean).addServlet(servlet);
			}
		}
	}

	/**
	 * Convert {@link org.ajax4jsf.resource.InternetResource } key to real URL
	 * for handle by chameleon filter, depend of mapping in WEB.XML . For prefix
	 * or * mapping, prepend servlet prefix and default Resource prefix to key.
	 * For suffix mapping, prepend with resource prefix and append default faces
	 * suffix to URL ( before request param ). After conversion, call
	 * {@link javax.faces.application.ViewHandler#getResourceURL(javax.faces.context.FacesContext, java.lang.String)}
	 * and
	 * {@link javax.faces.context.ExternalContext#encodeResourceURL(java.lang.String)} .
	 * 
	 * @param context
	 * @param Url
	 * @return
	 */
	public String getFacesResourceURL(FacesContext context, String Url) {
		StringBuffer buf = new StringBuffer();
		buf.append(getResourcePrefix()).append(Url);
		// Insert suffix mapping
		if (isPrefixMapping()) {
			buf.insert(0, getFacesFilterPrefix());
		} else {
			int index;
			if ((index = buf.indexOf("?")) >= 0) {
				buf.insert(index, getFacesFilterSuffix());
			} else {
				buf.append(getFacesFilterSuffix());
			}
		}
		String resourceURL = context.getApplication().getViewHandler()
				.getResourceURL(context, buf.toString());
		return resourceURL;

	}

	/**
	 * Detect request to resource and extract key from request
	 * 
	 * @param request
	 *            current http request
	 * @return resource key, or null for ordinary faces request.
	 */
	public String getFacesResourceKey(HttpServletRequest request) {
		String resourcePath = request.getRequestURI().substring(
				request.getContextPath().length());// isPrefixMapping()?request.getPathInfo():request.getServletPath();
		// Remove JSESSIONID - for expired sessions it will merged to path.
		int jsesionidStart;
		if ((jsesionidStart = resourcePath.lastIndexOf(";jsessionid")) >= 0) {
			resourcePath = resourcePath.substring(0, jsesionidStart);
		}
		if (isPrefixMapping()) {
			if (resourcePath.startsWith(getFacesFilterPrefix()
					+ getResourcePrefix())) {
				return resourcePath.substring(getFacesFilterPrefix().length()
						+ getResourcePrefix().length());
			}
		} else if (resourcePath.startsWith(getResourcePrefix())) {
			return resourcePath.substring(getResourcePrefix().length(),
					resourcePath.length() - getFacesFilterSuffix().length());
		}
		return null;
	}

	/**
	 * Detect request to {@link javax.faces.webapp.FacesServlet}
	 * 
	 * @param request
	 * @return true if request parsed to JSF.
	 */
	public boolean isFacesRequest(HttpServletRequest request) {
		// String resourcePath =
		// request.getRequestURI().substring(request.getContextPath().length());//isPrefixMapping()?request.getPathInfo():request.getServletPath();
		// if(isPrefixMapping() ) {
		// if (resourcePath.startsWith(getFacesFilterPrefix())) {
		// return true;
		// }
		// } else if (resourcePath.endsWith(getFacesFilterSuffix())) {
		// return true;
		// }
		// return false;
		return true;
	}

	/**
	 * @return Returns the facesFilterPrefix.
	 */
	public String getFacesFilterPrefix() {
		return _facesFilterPrefix;
	}

	/**
	 * @param facesFilterPrefix
	 *            The facesFilterPrefix to set.
	 */
	void setFacesFilterPrefix(String facesFilterPrefix) {
		_facesFilterPrefix = facesFilterPrefix;
	}

	/**
	 * @return Returns the facesFilterSuffix.
	 */
	public String getFacesFilterSuffix() {
		return _facesFilterSuffix;
	}

	/**
	 * @param facesFilterSuffix
	 *            The facesFilterSuffix to set.
	 */
	void setFacesFilterSuffix(String facesFilterSuffix) {
		_facesFilterSuffix = facesFilterSuffix;
	}

	/**
	 * @return Returns the resourcePrefix.
	 */
	public String getResourcePrefix() {
		return _resourcePrefix;
	}

	/**
	 * @param resourcePrefix
	 *            The resourcePrefix to set.
	 */
	void setResourcePrefix(String resourcePrefix) {
		_resourcePrefix = resourcePrefix;
	}

	/**
	 * @return Returns the filterName.
	 */
	public String getFilterName() {
		return _filterName;
	}

	/**
	 * After parsing web.xml set chameleon filter name, for wich we must
	 * calculate mappings for resources, logs etc.
	 * 
	 * @param filterName
	 *            The filterName to set.
	 * @param context
	 *            TODO
	 */
	void setFilterName(String filterName, ServletContext context) {
		if (null == filterName) {
			_log.warn(Messages.getMessage(Messages.NULL_FILTER_NAME_WARNING));
			return;
		}
		_filterName = filterName.trim();
		// get config for this filter
		FilterBean filter = (FilterBean) _filters.get(_filterName);
		if (null == filter) {
			_log.warn(Messages.getMessage(Messages.FILTER_NOT_FOUND_ERROR,
					_filterName));
			throw new IllegalStateException(Messages.getMessage(
					Messages.FILTER_NOT_FOUND_ERROR, filterName));
		}
		// find faces servlet
		Mapping mapping = checkMapping(filter.getMappings());
		// Filter mapped only to servlet.
		if (null == mapping) {
			for (Iterator<String> sevlets = filter.getServlets().iterator(); sevlets
					.hasNext()
					&& _facesFilterPrefix == null && _facesFilterSuffix == null;) {
				String servletname = sevlets.next();
				ServletBean servlet = (ServletBean) _servlets.get(servletname);
				if (null != servlet) {
					mapping = checkMapping(servlet.getMappings());
				}
			}
		}
		if (null != mapping) {
			setFacesFilterPrefix(mapping.getPrefix());
			setFacesFilterSuffix(mapping.getSuffix());
		} else {
			throw new IllegalStateException(Messages.getMessage(
					Messages.NO_PREFIX_OR_SUFFIX_IN_FILTER_MAPPING_ERROR,
					filterName));
		}
		String resourcePrefix = (String) context
				.getInitParameter(RESOURCE_URI_PREFIX_PARAM);
		if (null == resourcePrefix) {
			resourcePrefix = RESOURCE_URI_PREFIX;
		}
		if (null != _facesFilterPrefix) {
			_prefixMapping = true;
			if (_facesFilterPrefix.endsWith("/")) {
				setResourcePrefix(resourcePrefix);
			} else {
				setResourcePrefix("/" + resourcePrefix);
			}
		} else if (null != _facesFilterSuffix) {
			_prefixMapping = false;
			setResourcePrefix("/" + resourcePrefix);
		}
	}

	private Mapping checkMapping(Set<String> mappings) {
		Mapping mapping = null;
		if (null != mappings) {
			for (Iterator<String> iter = mappings.iterator(); iter.hasNext();) {
				String mappingPattern = (String) iter.next();
				// first test - for prefix, like /xxx/*
				// TODO - select correct dispatcher.
				if (mappingPattern.endsWith("*")) {
					if (null == mapping) {
						mapping = new Mapping();
					}
					int cut = mappingPattern.endsWith("/*")?2:1;
					mapping.setPrefix(mappingPattern.substring(0,
							mappingPattern.length() - cut));
					break;
				} else
				// test for suffix mapping, eg *.xxx
				if (mappingPattern.startsWith("*")) {
					if (null == mapping) {
						mapping = new Mapping();
					}
					mapping.setSuffix(mappingPattern.substring(1));
				} else {
					// Fixed mapping - do not use it.
				}
			}
		}
		return mapping;
	}

	/**
	 * @return Returns the prefixMapping.
	 */
	public boolean isPrefixMapping() {
		return _prefixMapping;
	}

	private static class Mapping {
		private String prefix;
		private String suffix;

		/**
		 * @return the prefix
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * @param prefix
		 *            the prefix to set
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		/**
		 * @return the suffix
		 */
		public String getSuffix() {
			return suffix;
		}

		/**
		 * @param suffix
		 *            the suffix to set
		 */
		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}
	}

	/**
	 * @return the facesServletPrefix
	 */
	public String getFacesServletPrefix() {
		return _facesServletPrefix;
	}

	/**
	 * @return the facesServletSuffix
	 */
	public String getFacesServletSuffix() {
		return _facesServletSuffix;
	}
}
