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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ajax4jsf.Messages;
import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.renderkit.AjaxContainerRenderer;
import org.ajax4jsf.request.MultipartRequest;
import org.ajax4jsf.resource.InternetResourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for request processing filters, with convert Htmp content to XML
 * for ajax requests, and serve request to application off-page resources
 * 
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:58:21 $
 * 
 */
public abstract class BaseFilter implements Filter {

	public static final String AJAX_PUSH_READY = "READY";

	public static final String AJAX_PUSH_STATUS_HEADER = "Ajax-Push-Status";

	public static final String AJAX_PUSH_KEY_HEADER = "Ajax-Push-Key";

	private static final Log log = LogFactory.getLog(BaseFilter.class);

	public static final boolean DEBUG = true;

	private FilterConfig filterConfig;

	private static final String FUNCTION_NAME_PARAMETER = "function";

	private String function = "alert('Data received');JSHttpRequest.dataReady";

	private String attributesNames;

	private boolean rewriteid = false;

	public static final String REWRITEID_PARAMETER = "rewriteid";

	public static final String STYLESHEET_PARAMETER = "xsl";

	public static final String ABSOLUTE_TAGS_PARAMETER = "absolute-attributes";

	// private WebXml webXml;
	// private String xsl;
	// private Templates xslTemplates;
	/**
	 * 
	 */
	private static final long serialVersionUID = -2295534611886142935L;

	public static final String DATA_PARAMETER = "DATA";

	public static final String DEFAULT_SERVLET_PATH = "/resource";

	public static final String RENDERER_PREFIX = "/renderer";

	public static final String CACHEABLE_PREFIX = "/cache";

	// private static final Pattern rendererPattern =
	// Pattern.compile(RENDERER_PREFIX+"/([^/]+)/([^/]+)/([^/]+)/(.*)");
	// private static final Pattern builderPattern =
	// Pattern.compile(CACHEABLE_PREFIX+"/(.*)");
	public static final String FILTER_PERFORMED = "com.exade.vcp.Filter.done";

	public static final String RESPONSE_WRAPPER_ATTRIBUTE = "com.exade.vcp.Filter.ResponseWrapper";

	protected BaseXMLFilter xmlFilter = null;

	protected InternetResourceService resourceService = null;

	protected PollEventsManager eventsManager;

	/**
	 * Flag indicating whether a temporary file should be used to cache the
	 * uploaded file
	 */
	private boolean createTempFiles = false;

	/**
	 * The maximum size of a file upload request. 0 means no limit.
	 */
	private int maxRequestSize = 0;
	
	/** Multipart request start */
	public static final String MULTIPART = "multipart/";

	/** Session bean name where multipart requests map will be stored */
	public static final String REQUESTS_SESSIONS_BEAN_NAME = "_richfaces_upload_sessions";

	/** Session bean name where progress bar's percent map will be stored */
	public static final String PERCENT_BEAN_NAME = "_richfaces_upload_percents";

	/**
	 * Request parameter that indicates if multipart request forced by rich file
	 * upload component
	 */
	public static final String UPLOAD_FILES_ID = "_richfaces_upload_uid";

	/** Session bean name to store max files count allowed to upload */
	public static final String UPLOADED_COUNTER = "_richfaces_uploaded_file_counter";
	
	/** Request parameter name indicated that file was uploaded by RF component */
	public static final String FILE_UPLOAD_INDICATOR = "_richfaces_upload_file_indicator";

	/**
	 * Initialize the filter.
	 */
	public void init(FilterConfig config) throws ServletException {
		if (log.isDebugEnabled()) {
			log.debug("Init ajax4jsf filter with nane: "
					+ config.getFilterName());
			Enumeration<String> parameterNames = config.getInitParameterNames();
			StringBuffer parameters = new StringBuffer("Init parameters :\n");
			while (parameterNames.hasMoreElements()) {
				String name = parameterNames.nextElement();
				parameters.append(name).append(" : '").append(
						config.getInitParameter(name)).append('\n');
			}
			log.debug(parameters);
			// log.debug("Stack Trace", new Exception());
		}
		// Save config
		filterConfig = config;
		setFunction((String) nz(filterConfig
				.getInitParameter(FUNCTION_NAME_PARAMETER), getFunction()));
		setAttributesNames(filterConfig
				.getInitParameter(ABSOLUTE_TAGS_PARAMETER));
		xmlFilter.init(config);
		if ("true".equalsIgnoreCase(filterConfig
				.getInitParameter(REWRITEID_PARAMETER))) {
			this.setRewriteid(true);
		}
		resourceService = new InternetResourceService();
		// Caching initialization.
		resourceService.init(filterConfig);
		eventsManager = new PollEventsManager();
		eventsManager.init(filterConfig.getServletContext());

		String param = filterConfig.getInitParameter("createTempFiles");
		if (param != null) {
			this.createTempFiles = Boolean.parseBoolean(param);
		} else {
			this.createTempFiles = true;
		}
		param = filterConfig.getInitParameter("maxRequestSize");
		if (param != null) {
			this.maxRequestSize = Integer.parseInt(param);
		}
	}

	private boolean isMultipartRequest(HttpServletRequest request) {
		if (!"post".equals(request.getMethod().toLowerCase())) {
			return false;
		}

		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}

		if (contentType.toLowerCase().startsWith(MULTIPART)) {
			return true;
		}

		return false;
	}

	private boolean isFileSizeRestricted(ServletRequest request, int maxSize) {
		if (maxSize != 0 && request.getContentLength() > maxSize) {
			return true;
		}
		return false;
	}

	private boolean checkFileCount(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Integer> map = (Map<String, Integer>) session
				.getAttribute(UPLOADED_COUNTER);
		if (map != null) {
			String id = request.getParameter("id");
			if (id != null) {
				Integer i = map.get(id);
				if (i != null && i == 0) {
					return false;
				}
			}
		}
		return true;
	}

	private void printResponse(ServletResponse response, String message)
			throws IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setStatus(HttpServletResponse.SC_OK);
		httpResponse.setContentType("text/html");
		PrintWriter writer = httpResponse.getWriter();
		writer.write(message);
		writer.close();
	}

	protected void handleRequest(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws IOException, ServletException {

		// check ajax request parameter
		// TODO - check for JSF page.
		if (true) {
			if (log.isDebugEnabled()) {
				log.debug(Messages
						.getMessage(Messages.FILTER_XML_OUTPUT));
			}

			// Execute the rest of the filter chain, including the
			// JSP
			xmlFilter.doXmlFilter(chain, request,
					response);
		} else {
			// normal request, execute chain ...
			if (log.isDebugEnabled()) {
				log.debug(Messages
						.getMessage(Messages.FILTER_NO_XML_CHAIN));
			}
			chain.doFilter(request, response);

		}
	
	}
	
	/**
	 * Method catches upload files request. Request parameter
	 * <b>org.ajax4jsf.Filter.UPLOAD_FILES_ID</b> indicates if request
	 * generated by rich-upload component. If it was detected custom parsing
	 * request should be done. Processing information about percent of
	 * completion and file size will be put into session scope. 
	 * 
	 * @param request
	 * @param response
	 * @return 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	protected void processUploadsAndHandleRequest(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest httpRequest = (HttpServletRequest) request;
	    String uid = httpRequest.getParameter(UPLOAD_FILES_ID);
	    if (uid != null) {
		if (isMultipartRequest(httpRequest)) {
		    MultipartRequest multipartRequest = new MultipartRequest(
			    httpRequest, createTempFiles, maxRequestSize, uid);

		    Map<String, MultipartRequest> sessionsMap = null;
		    Map<String, Object> percentMap = null;
		    try {
			if (isFileSizeRestricted(request, maxRequestSize)) {
			    printResponse(response,
			    "<html id=\"_richfaces_file_upload_size_restricted\"></html>");
			} else if (!checkFileCount(httpRequest)) {
			    printResponse(response,
			    "<html id=\"_richfaces_file_upload_forbidden\"></html>");
			} else {
			    HttpSession session = httpRequest.getSession();
			    synchronized (session) {
				sessionsMap = (Map<String, MultipartRequest>) session
				.getAttribute(REQUESTS_SESSIONS_BEAN_NAME);
				percentMap = (Map<String, Object>) session
				.getAttribute(PERCENT_BEAN_NAME);
				if (sessionsMap == null) {
				    sessionsMap = Collections
				    .synchronizedMap(new HashMap<String, MultipartRequest>());
				    session.setAttribute(
					    REQUESTS_SESSIONS_BEAN_NAME,
					    sessionsMap);
				}
				if (percentMap == null) {
				    percentMap = new HashMap<String, Object>();
				    session.setAttribute(PERCENT_BEAN_NAME,
					    percentMap);
				}
			    }
			    percentMap.put(uid, 0); // associate percent value with
			    // file
			    // entry uid
			    sessionsMap.put(uid, multipartRequest);

			    if (multipartRequest.parseRequest()) {
				handleRequest(multipartRequest, response, chain);
			    } else {
				printResponse(response,
					"<html id=\"_richfaces_file_upload_stopped\"></html>");
			    }
			}
		    } finally {
			if (sessionsMap != null) {
			    sessionsMap.remove(uid);
			    percentMap.remove(uid);
			}
		    }
		} else {
		    if ("stop".equals(httpRequest.getParameter("action"))) {
			HttpSession session = httpRequest.getSession();
			Map<String, MultipartRequest> sessions = (Map<String, MultipartRequest>) session
			.getAttribute(REQUESTS_SESSIONS_BEAN_NAME);

			if (sessions != null) {
			    MultipartRequest multipartRequest = sessions.get(uid);
			    if (multipartRequest != null) {
				multipartRequest.stop();
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse
				.setStatus(HttpServletResponse.SC_NO_CONTENT);
				httpResponse.getOutputStream().close();
			    }
			}
			
			//TODO what's here?
		    } else {
			handleRequest(request, response, chain);
		    }
		}
	    } else {
		handleRequest(request, response, chain);
	    }
	}
	
	/**
	 * @param httpServletRequest
	 * @throws UnsupportedEncodingException
	 */
	protected void setupRequestEncoding(HttpServletRequest httpServletRequest)
			throws UnsupportedEncodingException {
		String contentType = httpServletRequest.getHeader("Content-Type");

		String characterEncoding = lookupCharacterEncoding(contentType);

		if (characterEncoding == null) {
			HttpSession session = httpServletRequest.getSession(false);

			if (session != null) {
				characterEncoding = (String) session
						.getAttribute(ViewHandler.CHARACTER_ENCODING_KEY);
			}

			if (characterEncoding != null) {
				httpServletRequest.setCharacterEncoding(characterEncoding);
			}
		}
	}

	/**
	 * Detect request encoding from Content-Type header
	 * 
	 * @param contentType
	 * @return - charset, if present.
	 */
	private String lookupCharacterEncoding(String contentType) {
		String characterEncoding = null;

		if (contentType != null) {
			int charsetFind = contentType.indexOf("charset=");
			if (charsetFind != -1) {
				if (charsetFind == 0) {
					// charset at beginning of Content-Type, curious
					characterEncoding = contentType.substring(8);
				} else {
					char charBefore = contentType.charAt(charsetFind - 1);
					if (charBefore == ';' || Character.isWhitespace(charBefore)) {
						// Correct charset after mime type
						characterEncoding = contentType
								.substring(charsetFind + 8);
					}
				}
				if (log.isDebugEnabled())
					log.debug(Messages.getMessage(
							Messages.CONTENT_TYPE_ENCODING, characterEncoding));
			} else {
				if (log.isDebugEnabled())
					log.debug(Messages.getMessage(
							Messages.CONTENT_TYPE_NO_ENCODING, contentType));
			}
		}
		return characterEncoding;
	}

	/**
	 * @param initParameter
	 * @param function2
	 * @return
	 */
	private Object nz(Object param, Object def) {
		return param != null ? param : def;
	}

	/**
	 * Execute the filter.
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		long startTimeMills = 0;
		// Detect case of request - normal, AJAX, AJAX - JavaScript
		// TODO - detect first processing in filter.
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if (log.isDebugEnabled()) {
			startTimeMills = System.currentTimeMillis();
			log.debug(Messages.getMessage(Messages.FILTER_START_INFO, new Date(
					startTimeMills), httpServletRequest.getRequestURI()));
		}

		if (request.getAttribute(FILTER_PERFORMED) != Boolean.TRUE) {
			// mark - and not processing same request twice.
			try {
				request.setAttribute(FILTER_PERFORMED, Boolean.TRUE);
				String ajaxPushHeader = httpServletRequest
						.getHeader(AJAX_PUSH_KEY_HEADER);
				// check for a push check request.
				if (httpServletRequest.getMethod().equals("HEAD")
						&& null != ajaxPushHeader) {
					PushEventsCounter listener = eventsManager
							.getListener(ajaxPushHeader);
					// To avoid XmlHttpRequest parsing exceptions.
					httpServletResponse.setContentType("text/plain");
					if (listener.isPerformed()) {
						listener.processed();
						httpServletResponse.setStatus(HttpServletResponse.SC_OK);
						httpServletResponse.setHeader(AJAX_PUSH_STATUS_HEADER, AJAX_PUSH_READY);
						if (log.isDebugEnabled()) {
							log
									.debug("Occurs event for a id "
											+ ajaxPushHeader);
						}
					} else {
						// Response code - 'No content'
						httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
						if (log.isDebugEnabled()) {
							log.debug("No event for a id " + ajaxPushHeader);
						}
					}
					httpServletResponse.setContentLength(0);
				} else
				// check for resource request
				if (!getResourceService().serviceResource(httpServletRequest,
						httpServletResponse)) {
					// Not request to resource - perform filtering.
					// first stage - detect/set encoding of request. Same as in
					// Myfaces External Context.
					setupRequestEncoding(httpServletRequest);
					
					processUploadsAndHandleRequest(httpServletRequest, httpServletResponse, chain);
				}
			} finally {
				// Remove filter marker from response, to enable sequence calls ( for example, forward to error page )
				request.removeAttribute(FILTER_PERFORMED);
				Object ajaxContext = request.getAttribute(AjaxContext.AJAX_CONTEXT_KEY);
				if(null != ajaxContext && ajaxContext instanceof AjaxContext){
					((AjaxContext)ajaxContext).release();
					request.removeAttribute(AjaxContext.AJAX_CONTEXT_KEY);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage(Messages.FILTER_NO_XML_CHAIN_2));
			}
			chain.doFilter(request, response);

		}
		if (log.isDebugEnabled()) {
			startTimeMills = System.currentTimeMillis() - startTimeMills;
			log.debug(Messages.getMessage(Messages.FILTER_STOP_INFO, ""
					+ startTimeMills, httpServletRequest.getRequestURI()));
		}
	}

	/**
	 * @param request
	 * @return
	 */
	protected boolean isAjaxRequest(ServletRequest request) {
		try {
			return null != request
					.getParameter(AjaxContainerRenderer.AJAX_PARAMETER_NAME);
		} catch (Exception e) {
			// OCJ 10 - throw exception for static resources.
			return false;
		}
	}

	/**
	 * Destroy the filter.
	 */
	public void destroy() {
	}

	/**
	 * @return Returns the servletContext.
	 */
	ServletContext getServletContext() {
		return filterConfig.getServletContext();
	}

	/**
	 * @return the resourceService
	 * @throws ServletException
	 */
	protected synchronized InternetResourceService getResourceService()
			throws ServletException {
		// if (resourceService == null) {
		// resourceService = new InternetResourceService();
		// // Caching initialization.
		// resourceService.init(filterConfig);
		//
		// }
		return resourceService;
	}

	/**
	 * @param function
	 *            The function to set.
	 */
	protected void setFunction(String function) {
		this.function = function;
	}

	/**
	 * @return Returns the function.
	 */
	protected String getFunction() {
		return function;
	}

	/**
	 * @param rewriteid
	 *            The rewriteid to set.
	 */
	protected void setRewriteid(boolean rewriteid) {
		this.rewriteid = rewriteid;
	}

	/**
	 * @return Returns the rewriteid.
	 */
	protected boolean isRewriteid() {
		return rewriteid;
	}

	/**
	 * @param attributesNames
	 *            The attributesNames to set.
	 */
	protected void setAttributesNames(String attributesNames) {
		this.attributesNames = attributesNames;
	}

	/**
	 * @return Returns the attributesNames.
	 */
	protected String getAttributesNames() {
		return attributesNames;
	}
}
