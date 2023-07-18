package org.ajax4jsf.context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.ajax4jsf.resource.util.URLToStreamHelper;

public abstract class AjaxContext {

	public static final String SCRIPTS_PARAMETER = "org.ajax4jsf.framework.HEADER_SCRIPTS";
	public static final String STYLES_PARAMETER = "org.ajax4jsf.framework.HEADER_STYLES";
	public static final String USER_STYLES_PARAMETER = "org.ajax4jsf.framework.HEADER_USER_STYLES";
	public static final String RESPONSE_DATA_KEY = "_ajax:data";
	static final String SERVICE_RESOURCE = "META-INF/services/"
			+ AjaxContext.class.getName();
	private static final String DEFAULT_CONTEXT_CLASS = "org.ajax4jsf.context.AjaxContextImpl";

	/**
	 * Key for keep request state information in request-scope attributes.
	 */
	public static final String AJAX_CONTEXT_KEY = "ajaxContext";

	public abstract Map<String, Object> getCommonAjaxParameters();

	public abstract String getAjaxActionURL(FacesContext context);

	public abstract void setResponseData(Object responseData);

	public abstract Object getResponseData();

	public abstract void setOncomplete(Object oncompleteFunction);

	public abstract Object getOncomplete();

	public abstract void setViewIdHolder(ViewIdHolder viewIdHolder);

	public abstract ViewIdHolder getViewIdHolder();

	public abstract boolean removeRenderedArea(String id);

	public abstract void addRenderedArea(String id);

	public abstract Set<String> getAjaxRenderedAreas();

	public abstract void addComponentToAjaxRender(UIComponent base, String id);

	public abstract void addComponentToAjaxRender(UIComponent component);

	public abstract void addRegionsFromComponent(UIComponent component);

	public abstract Set<String> getAjaxAreasToRender();

	public abstract Set<String> getAjaxAreasToProcess();
	
	public boolean isAjaxRequest(FacesContext facesContext) {
		return isAjaxRequest();
	}

	public abstract boolean isAjaxRequest();

	public abstract void processHeadResources(FacesContext context)
			throws FacesException;

	public abstract void encodeAjaxEnd(FacesContext context) throws IOException;

	public abstract void encodeAjaxBegin(FacesContext context) throws IOException;



	public abstract void renderAjax(FacesContext context);

	public abstract void decode(FacesContext context);

	public abstract void release();

	public abstract Map<String, Object> getResponseDataMap();

	public abstract void setAjaxRequest(boolean b);

	public abstract boolean isSelfRender();

	public abstract void setSelfRender(boolean b);

	public abstract String getSubmittedRegionClientId();

	public abstract void saveViewState(FacesContext context) throws IOException;

	public abstract void setAjaxSingleClientId(String ajaxSingleClientId);

	public abstract String getAjaxSingleClientId();

	public abstract void setAjaxAreasToProcess(Set<String> ajaxAreasToProcess);

	/**
	 * Get instance of current AJAX Context. Instance get by
	 * variable {@link AjaxContext#AJAX_CONTEXT_KEY}
	 * 
	 * @return memento instance for current request
	 */
	public static AjaxContext getCurrentInstance() {
		FacesContext context = FacesContext.getCurrentInstance();
		return getCurrentInstance(context);
	}

	private static Map<ClassLoader, Class<? extends AjaxContext>> ajaxContextClasses = new HashMap<ClassLoader, Class<? extends AjaxContext>>();

	/**
	 * Get instance of current AJAX Context. Instance get by
	 * variable {@link AjaxContext#AJAX_CONTEXT_KEY}
	 * 
	 * @param context
	 *            current FacesContext
	 * @return instance of AjaxContext.
	 */
	public static AjaxContext getCurrentInstance(FacesContext context) {
		if (null == context) {
			throw new NullPointerException("FacesContext is null");
		}
		Map<String, Object> requestMap = context.getExternalContext()
				.getRequestMap();
		AjaxContext ajaxContext = (AjaxContext) requestMap
				.get(AJAX_CONTEXT_KEY);
		if (null == ajaxContext) {
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			if(null == contextClassLoader) {
				contextClassLoader = AjaxContext.class.getClassLoader();
			}
			Class<? extends AjaxContext> clazz;
			synchronized (ajaxContextClasses) {
				clazz = ajaxContextClasses.get(contextClassLoader);
				if (null == clazz) {
					String factoryClassName = DEFAULT_CONTEXT_CLASS;
					// Pluggable factories.
					InputStream input = null; // loader.getResourceAsStream(SERVICE_RESOURCE);
					input = URLToStreamHelper.urlToStreamSafe(contextClassLoader
							.getResource(SERVICE_RESOURCE));
					// have services file.
					if (input != null) {
						try {
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(input));
							factoryClassName = reader.readLine();

						} catch (Exception e) {
							throw new FacesException(
									"Error to create AjaxContext Instance", e);
						} finally {
							try {
								input.close();
							} catch (IOException e) {
								// Ignore
							}
						}
					}
					try {
						clazz =  Class.forName(factoryClassName, false, contextClassLoader).asSubclass(AjaxContext.class);
					} catch (ClassNotFoundException e) {
						throw new FacesException(
								"AjaxContext implementation class "
										+ factoryClassName + " not found ", e);
					}
					ajaxContextClasses.put(contextClassLoader, clazz);
				}
			}
			try {
				ajaxContext =  clazz.newInstance();
				ajaxContext.decode(context);
			} catch (InstantiationException e) {
				throw new FacesException(
						"Error to create AjaxContext Instance", e);
			} catch (IllegalAccessException e) {
				throw new FacesException(
						"No access to AjaxContext constructor", e);
			}
			requestMap.put(AJAX_CONTEXT_KEY, ajaxContext);
		}
		return ajaxContext;
	}

	public AjaxContext() {
	}
	
}