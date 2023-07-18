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

package org.ajax4jsf.context;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.application.StateManager.SerializedView;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.ajax4jsf.Messages;
import org.ajax4jsf.application.AjaxViewHandler;
import org.ajax4jsf.renderkit.AjaxContainerRenderer;
import org.ajax4jsf.renderkit.AjaxRendererUtils;
import org.ajax4jsf.renderkit.RendererUtils;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.ajax4jsf.resource.InternetResourceBuilder;
import org.ajax4jsf.resource.ResourceNotFoundException;
import org.ajax4jsf.util.ELUtils;
import org.ajax4jsf.webapp.BaseFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.richfaces.skin.Skin;
import org.richfaces.skin.SkinFactory;
import org.richfaces.skin.SkinNotFoundException;

/**
 * This class incapsulated
 * 
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.7 $ $Date: 2007/02/08 19:07:16 $
 * 
 */
public class AjaxContextImpl extends AjaxContext {
	
	public static final String SERVLET_ERROR_EXCEPTION_ATTRIBUTE = "javax.servlet.error.exception";

	public static final String RESOURCES_PROCESSED = "org.ajax4jsf.framework.HEADER_PROCESSED";

	//we do not apply extended CSS styling for control for Opera & Safari 
	//as they have their own advanced styling
	private static final Pattern USER_AGENTS = Pattern.compile(" AppleWebKit/|^Opera/| Opera ");
	
	private static final String INIT_PARAMETER_PREFIX = "_init_parameter_";
	
	//Object to put into application map as a value of cached parameter if it is null
	private static final Object NULL = new Object();
	
	private static final Log log = LogFactory.getLog(AjaxContext.class);


	Set<String> ajaxAreasToRender = new HashSet<String>();

	Set<String> ajaxAreasToProcess = null;

	Set<String> ajaxRenderedAreas = new LinkedHashSet<String>();

	boolean ajaxRequest = false;

	boolean selfRender = false;

	Integer viewSequence = new Integer(1);

	String submittedRegionClientId = null;
	
	String ajaxSingleClientId = null;


	ViewIdHolder viewIdHolder = null;

	Map<String, Object> responseDataMap = new HashMap<String, Object> ();

	Map<String, Object>  commonAjaxParameters = new HashMap<String, Object> ();

	Object oncomplete = null;


	
	@PreDestroy
	public void release() {

		ajaxAreasToRender = new HashSet<String>();
		
		ajaxAreasToProcess = null;

		ajaxRenderedAreas = new LinkedHashSet<String>();

		ajaxRequest = false;

		selfRender = false;

		viewSequence = new Integer(1);

		submittedRegionClientId = null;

		viewIdHolder = null;

		responseDataMap = new HashMap<String, Object>();

		commonAjaxParameters = new HashMap<String, Object>();

	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.context.AjaxContext#decode(javax.faces.context.FacesContext)
	 */
	@Override
	@PostConstruct
	public void decode(FacesContext context) {
		ExternalContext externalContext = context.getExternalContext();
		if (null == externalContext.getRequestMap().get(
				SERVLET_ERROR_EXCEPTION_ATTRIBUTE)) {
			Map<String, String> requestParameterMap = externalContext
					.getRequestParameterMap();
			String ajaxRegionId = requestParameterMap
					.get(AjaxContainerRenderer.AJAX_PARAMETER_NAME);
			setSubmittedRegionClientId(ajaxRegionId);
			setAjaxRequest(null != ajaxRegionId);
			setAjaxSingleClientId(requestParameterMap.get(AjaxRendererUtils.AJAX_SINGLE_PARAMETER_NAME));
		} else {
			// Error page is always serviced as non-ajax.
			setAjaxRequest(false);
			setSubmittedRegionClientId(null);
			setAjaxSingleClientId(null);
		}
	}
	

	/**
	 * @param context
	 * @throws AbortProcessingException
	 */
	public void renderAjax(FacesContext context) throws FacesException {
		if (log.isDebugEnabled()) {
			log.debug(Messages.getMessage(Messages.RENDER_AJAX_REQUEST,
					getSubmittedRegionClientId()));
		}
		try {
			// Just in case...
			setSelfRender(true);
			setAjaxRequest(true);
			// create response writer.
			ExternalContext extContext = context.getExternalContext();
			RenderKit renderKit = context.getRenderKit();
			String encoding;
			// Depends if we talk about servlets, portlets, ...
			if (extContext.getRequest() instanceof ServletRequest) {
				ServletRequest request = (ServletRequest) extContext
						.getRequest();
				ServletResponse response = (ServletResponse) extContext
						.getResponse();
				// Setup encoding and content type
				String contentType = "text/xml";
				// get the encoding - must be setup by faces context or filter.
				encoding = request.getCharacterEncoding();
				if (encoding == null) {
					encoding = "UTF-8";
				}
				response.setContentType(contentType + ";charset=" + encoding);
			} else
				encoding = "UTF-8";

			PrintWriter servletWriter;
				servletWriter = getWriter(extContext);
			ResponseWriter writer = renderKit.createResponseWriter(
					servletWriter, null, encoding);
			context.setResponseWriter(writer);
			// make response
			writer.startDocument();
			encodeAjaxBegin(context);
			context.getViewRoot().encodeAll(context);
			saveViewState(context);
			encodeAjaxEnd(context);
			writer.endDocument();
			writer.flush();
			writer.close();
			servletWriter.close();
			// Save tree state.
		} catch (IOException e) {
			throw new FacesException(Messages.getMessage(
					Messages.RENDERING_AJAX_REGION_ERROR, getSubmittedRegionClientId()), e);
		} finally {
			context.responseComplete();
			// component.setRendererType(defaultRenderer);
		}
	}

	/**
	 * Encode declaration for AJAX response. Render &lt;html&gt;&lt;body&gt;
	 * 
	 * @param context
	 * @throws IOException
	 */
	public void encodeAjaxBegin(FacesContext context)
			throws IOException {
		UIViewRoot viewRoot = context.getViewRoot();
		// AjaxContainer ajax = (AjaxContainer) component;
		ResponseWriter out = context.getResponseWriter();
		// DebugUtils.traceView("ViewRoot in AJAX Page encode begin");
		out.startElement(HTML.HTML_ELEMENT, viewRoot);
		Locale locale = viewRoot.getLocale();
		out.writeAttribute(HTML.lang_ATTRIBUTE, locale.toString(), "lang");
		out.startElement(HTML.BODY_ELEMENT, viewRoot);
	}

	/**
	 * End encoding of AJAX response. Render tag with included areas and close
	 * &lt;/body&gt;&lt;/html&gt;
	 * 
	 * @param context
	 * @throws IOException
	 */
	public void encodeAjaxEnd(FacesContext context)
			throws IOException {
		// AjaxContainer ajax = (AjaxContainer) component;
		ResponseWriter out = context.getResponseWriter();
		// DebugUtils.traceView("ViewRoot in AJAX Page encode begin");
		out.endElement(HTML.BODY_ELEMENT);
		out.endElement(HTML.HTML_ELEMENT);
	}

	/**
	 * @param context
	 * @param root
	 * @throws FacesException
	 */
	public void processHeadResources(FacesContext context)
			throws FacesException {
		ExternalContext externalContext = context.getExternalContext();
		Map<String,Object> requestMap = externalContext.getRequestMap();
		if (!Boolean.TRUE.equals(requestMap.get(RESOURCES_PROCESSED))) {
			if (null != requestMap.get(BaseFilter.RESPONSE_WRAPPER_ATTRIBUTE)) {
				if (log.isDebugEnabled()) {
					log
							.debug("Process component tree for collect used scripts and styles");
				}
				UIViewRoot root = context.getViewRoot();
				ViewResources viewResources = new ViewResources();
				String skinStyleSheetUri = null;
				String skinExtendedStyleSheetUri = null;
				try {
					Skin skin = SkinFactory.getInstance().getSkin(context);
					// Set default style sheet for current skin.
					skinStyleSheetUri = (String) skin.getParameter(context,
							Skin.generalStyleSheet);
					// Set default style sheet for current skin.
					skinExtendedStyleSheetUri = (String) skin.getParameter(context,
							Skin.extendedStyleSheet);
					// For a "NULL" skin, do not collect components stylesheets
					if ("false".equals(skin.getParameter(context,
							Skin.loadStyleSheets))) {
						viewResources.setProcessStyles(false);
					}
				} catch (SkinNotFoundException e) {
					log.warn("Current Skin is not found", e);
				}
				InternetResourceBuilder internetResourceBuilder = InternetResourceBuilder
						.getInstance();
				// Check init parameters for a resources processing.
				String scriptStrategy = externalContext
						.getInitParameter(InternetResourceBuilder.LOAD_SCRIPT_STRATEGY_PARAM);
				if (null != scriptStrategy) {
					if (InternetResourceBuilder.LOAD_NONE
							.equals(scriptStrategy)) {
						viewResources.setProcessScripts(false);
					} else if (InternetResourceBuilder.LOAD_ALL
							.equals(scriptStrategy)) {
						viewResources.setProcessScripts(false);
						// For an "ALL" strategy, it is not necessary to load scripts in the ajax request
						if (!this.isAjaxRequest(context)) {
							try {
								viewResources
										.addScript(internetResourceBuilder
												.createResource(
														this,
														InternetResourceBuilder.COMMON_FRAMEWORK_SCRIPT)
												.getUri(context, null));
								viewResources
										.addScript(internetResourceBuilder
												.createResource(
														this,
														InternetResourceBuilder.COMMON_UI_SCRIPT)
												.getUri(context, null));

							} catch (ResourceNotFoundException e) {
								if (log.isWarnEnabled()) {
									log
											.warn("No aggregated javaScript library found "
													+ e.getMessage());
								}
							}

						}
					}
				}

				boolean useStdControlsSkinning = false;

				String stdControlsSkinning = getInitParameterValue(context, InternetResourceBuilder.STD_CONTROLS_SKINNING_PARAM);
				if (stdControlsSkinning != null) {
					useStdControlsSkinning = InternetResourceBuilder.ENABLE.equals(stdControlsSkinning);
				}
				
				boolean useStdControlsSkinningClasses = true;

				String stdControlsSkinningClasses = getInitParameterValue(context, InternetResourceBuilder.STD_CONTROLS_SKINNING_CLASSES_PARAM);
				if (stdControlsSkinningClasses != null) {
					useStdControlsSkinningClasses = InternetResourceBuilder.ENABLE.equals(stdControlsSkinningClasses);
				}
				
				boolean useExtendedSkinning = isExtendedSkinningEnabled(externalContext);
				
				String styleStrategy = externalContext
						.getInitParameter(InternetResourceBuilder.LOAD_STYLE_STRATEGY_PARAM);
				
				if (InternetResourceBuilder.LOAD_NONE.equals(styleStrategy)) {
					viewResources.setProcessStyles(false);
				} else if (InternetResourceBuilder.LOAD_ALL
						.equals(styleStrategy)) {
					viewResources.setProcessStyles(false);
					// For an "ALL" strategy, it is not necessary to load styles
					// in the ajax request
					if (!this.isAjaxRequest(context)) {
						String commonStyle = InternetResourceBuilder.COMMON_STYLE_PREFIX;

						if (useStdControlsSkinning
								|| useStdControlsSkinningClasses) {
							if (useExtendedSkinning) {
								commonStyle += "-ext";
							} else {
								commonStyle += "-bas";
							}

							if (useStdControlsSkinning
									&& useStdControlsSkinningClasses) {
								commonStyle += "-both";
							} else if (useStdControlsSkinning) {
								commonStyle += "-styles";
							} else if (useStdControlsSkinningClasses) {
								commonStyle += "-classes";
							}
						}

						commonStyle += InternetResourceBuilder.COMMON_STYLE_EXTENSION;

						try {
							viewResources.addStyle(internetResourceBuilder
									.createResource(this, commonStyle).getUri(
											context, null));

						} catch (ResourceNotFoundException e) {
							if (log.isWarnEnabled()) {
								log.warn("No aggregated stylesheet found "
										+ e.getMessage());
							}
						}

					}
				} else {
					if (useStdControlsSkinning) {
						viewResources.addStyle(
								internetResourceBuilder.createResource(
										this, InternetResourceBuilder.STD_CONTROLS_BASIC_STYLE)
										.getUri(context, null));
						
						if (useExtendedSkinning) {
							viewResources.addStyle(
									internetResourceBuilder.createResource(
											this, InternetResourceBuilder.STD_CONTROLS_EXTENDED_STYLE)
											.getUri(context, null));
						}
					}
					
					if (useStdControlsSkinningClasses) {
						viewResources.addStyle(
								internetResourceBuilder.createResource(
										this, InternetResourceBuilder.STD_CONTROLS_BASIC_CLASSES_STYLE)
										.getUri(context, null));
						
						if (useExtendedSkinning) {
							viewResources.addStyle(
									internetResourceBuilder.createResource(
											this, InternetResourceBuilder.STD_CONTROLS_EXTENDED_CLASSES_STYLE)
											.getUri(context, null));
						}
					}
				}

				viewResources.collect(context);
				Set<String> scripts = viewResources.getScripts();
				if (scripts.size() > 0) {
					if (log.isDebugEnabled()) {
						StringBuffer buff = new StringBuffer(
								"Scripts for insert into head : \n");
						for (Iterator<String> iter = scripts.iterator(); iter.hasNext();) {
							String script = iter.next();
							buff.append(script).append("\n");
						}
						log.debug(buff.toString());
					}
					requestMap.put(SCRIPTS_PARAMETER, scripts);
				}
				
				Set<String> styles = viewResources.getStyles();
				// Append Skin StyleSheet after a
				if (null != skinStyleSheetUri) {
					String resourceURL = context.getApplication()
							.getViewHandler().getResourceURL(context,
									skinStyleSheetUri);
					styles.add(resourceURL);
				}
				
				if (null != skinExtendedStyleSheetUri && useExtendedSkinning) {
					String resourceURL = context.getApplication().getViewHandler().getResourceURL(context,
							skinExtendedStyleSheetUri);
					styles.add(resourceURL);
				}
				if (styles.size() > 0) {
					if (log.isDebugEnabled()) {
						StringBuffer buff = new StringBuffer(
								"Styles for insert into head : \n");
						for (Iterator<String> iter = styles.iterator(); iter.hasNext();) {
							String style = (String) iter.next();
							buff.append(style).append("\n");
						}
						log.debug(buff.toString());
					}
					requestMap.put(STYLES_PARAMETER, styles);
				}
				
				Set<String> usersStyles = viewResources.getUserStyles();
				if (usersStyles.size() > 0) {
					if (log.isDebugEnabled()) {
						StringBuffer buff = new StringBuffer(
								"User styles for insert into head : \n");
						for (Iterator<String> iter = usersStyles.iterator(); iter.hasNext();) {
							String style = (String) iter.next();
							buff.append(style).append("\n");
						}
						log.debug(buff.toString());
					}
					requestMap.put(USER_STYLES_PARAMETER, usersStyles);
				}
				// Mark as processed.
				requestMap.put(RESOURCES_PROCESSED, Boolean.TRUE);
				// Save viewId for a parser selection
				requestMap.put(AjaxViewHandler.VIEW_ID_KEY, root.getViewId());
			}

		}
	}
	
	@SuppressWarnings("deprecation")
	public void saveViewState(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		StateManager stateManager = context.getApplication().getStateManager();
		SerializedView serializedView = stateManager
				.saveSerializedView(context);
		if (null != serializedView && null != writer) {
			StringWriter bufWriter = new StringWriter();
			ResponseWriter tempWriter;
			tempWriter = writer.cloneWithWriter(bufWriter);
			context.setResponseWriter(tempWriter);
			stateManager.writeState(context, serializedView);
			tempWriter.flush();
			if (bufWriter.getBuffer().length() > 0) {
				context.getExternalContext().getRequestMap().put(
						AjaxViewHandler.SERIALIZED_STATE_KEY,
						bufWriter.toString());
			}
			context.setResponseWriter(writer);
		}
	}

	protected RenderKit getRenderKit(FacesContext context) {
		RenderKit renderKit = context.getRenderKit();
		if (null == renderKit) {
			String renderKitId = context.getApplication().getViewHandler().calculateRenderKitId(context);
			RenderKitFactory factory = (RenderKitFactory) FactoryFinder
					.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
			renderKit = factory.getRenderKit(context, renderKitId);
		}
		return renderKit;
	}

	/**
	 * @return Returns the ajaxRequest.
	 */
	public boolean isAjaxRequest() {
		return ajaxRequest;
	}

	/**
	 * @param ajaxRequest
	 *            The ajaxRequest to set.
	 */
	public void setAjaxRequest(boolean ajaxRequest) {
		this.ajaxRequest = ajaxRequest;
	}

	/**
	 * @return Returns the ajaxAreasToRender.
	 */
	public Set<String> getAjaxAreasToRender() {
		return this.ajaxAreasToRender;
	}

	/**
	 * @return the ajaxAreasToProcess
	 */
	@Override
	public Set<String> getAjaxAreasToProcess() {
		return ajaxAreasToProcess;
	}

	/**
	 * @param ajaxAreasToProcess the ajaxAreasToProcess to set
	 */
	@Override
	public void setAjaxAreasToProcess(Set<String> ajaxAreasToProcess) {
		this.ajaxAreasToProcess = ajaxAreasToProcess;
	}

	/**
	 * Add affected regions's ID to ajaxView component.
	 * 
	 * @param component
	 */
	public void addRegionsFromComponent(UIComponent component) {
		// First step - find parent ajax view
		Set<String> ajaxRegions = AjaxRendererUtils.getAjaxAreas(component);
		// if (ajaxRegions == null){
		// FacesContext context = FacesContext.getCurrentInstance();
		// ajaxRegions = AjaxRendererUtils.getAbsoluteId(context,component);
		// }
		if (log.isDebugEnabled()) {
			log.debug(Messages.getMessage(Messages.INVOKE_AJAX_REGION_LISTENER,
					component.getId()));
		}
		if (ajaxRegions != null) {
			for (Iterator<String> iter = ajaxRegions.iterator(); iter.hasNext();) {
				String id = iter.next().toString();
				ajaxAreasToRender.add(convertId(component, id));
			}
		}
	}

	public void addComponentToAjaxRender(UIComponent component) {
		this.ajaxAreasToRender.add(AjaxRendererUtils.getAbsoluteId(component));
	}

	public void addComponentToAjaxRender(UIComponent base, String id) {
		this.ajaxAreasToRender.add(convertId(base, id));
	}

	/**
	 * Test for relative id of target components. Attempt convert to absolute.
	 * For use as argument for
	 * {@link RendererUtils#findComponentFor(UIComponent, String)}
	 * 
	 * @param component
	 * @param id
	 * @return
	 */
	private String convertId(UIComponent component, String id) {
		if (id.charAt(0) == NamingContainer.SEPARATOR_CHAR) {
			return id;
		}
		if (null == component) {
			throw new NullPointerException(
					"Base component for search re-rendered compnnent is null");
		}
		UIComponent target = RendererUtils.getInstance().findComponentFor(
				component, id);
		if (null != target) {
			return AjaxRendererUtils.getAbsoluteId(target);
		}
		log.warn("Target component for id " + id + " not found");
		return id;
	}

	/**
	 * @return Returns the ajaxRenderedAreas.
	 */
	public Set<String> getAjaxRenderedAreas() {
		return ajaxRenderedAreas;
	}

	public void addRenderedArea(String id) {
		ajaxRenderedAreas.add(id);
	}

	public boolean removeRenderedArea(String id) {
		return ajaxRenderedAreas.remove(id);
	}

	/**
	 * @return Returns the submittedClientId.
	 */
	public String getSubmittedRegionClientId() {
		return this.submittedRegionClientId;
	}

	/**
	 * @param submittedClientId
	 *            The submittedClientId to set.
	 */
	public void setSubmittedRegionClientId(String submittedClientId) {
		this.submittedRegionClientId = submittedClientId;
	}

	/**
	 * @return the ajaxSingleClientId
	 */
	@Override
	public String getAjaxSingleClientId() {
		return ajaxSingleClientId;
	}

	/**
	 * @param ajaxSingleClientId the ajaxSingleClientId to set
	 */
	@Override
	public void setAjaxSingleClientId(String ajaxSingleClientId) {
		this.ajaxSingleClientId = ajaxSingleClientId;
	}

	/**
	 * @return Returns the selfRender.
	 */
	public boolean isSelfRender() {
		return selfRender;
	}

	/**
	 * @param selfRender
	 *            The selfRender to set.
	 */
	public void setSelfRender(boolean selfRender) {
		this.selfRender = selfRender;
	}

	/**
	 * @return the vievIdHolder
	 */
	public ViewIdHolder getViewIdHolder() {
		return viewIdHolder;
	}

	/**
	 * @param viewIdHolder
	 *            the vievIdHolder to set
	 */
	public void setViewIdHolder(ViewIdHolder viewIdHolder) {
		this.viewIdHolder = viewIdHolder;
	}

	/**
	 * @return the responseData
	 */
	public Object getResponseData() {
		return responseDataMap.get(RESPONSE_DATA_KEY);
	}

	/**
	 * @param responseData
	 *            the responseData to set
	 */
	public void setResponseData(Object responseData) {
		this.responseDataMap.put(RESPONSE_DATA_KEY, responseData);
	}

	/**
	 * @return the responseDataMap
	 */
	public Map<String, Object> getResponseDataMap() {
		return responseDataMap;
	}

	/**
	 * Gives back the writer of a Response object.
	 * 
	 * @param extContext
	 *            The external context.
	 * @return The writer of the response.
	 * @throws FacesException
	 *             If the response object has no getWriter() method.
	 */
	protected PrintWriter getWriter(ExternalContext extContext)
			throws FacesException {
		PrintWriter writer = null;
		Object response = extContext.getResponse();
		try {
			Method gW = response.getClass()
					.getMethod("getWriter", new Class[0]);
			writer = (PrintWriter) gW.invoke(response, new Object[0]);
		} catch (Exception e) {
			throw new FacesException(e);
		}
		return writer;
	}


	public String getAjaxActionURL(FacesContext context) {
		// Check arguments
		if (null == context) {
			throw new NullPointerException(
					"Faces context for build AJAX Action URL is null");
		}
		UIViewRoot viewRoot = context.getViewRoot();
		if (null == viewRoot) {
			throw new NullPointerException(
					"Faces view tree for build AJAX Action URL is null");
		}
		String viewId = viewRoot.getViewId();
		if (null == viewId) {
			throw new NullPointerException(
					"View id for build AJAX Action URL is null");
		}
		if (!viewId.startsWith("/")) {
			throw new IllegalArgumentException(
					"Illegal view Id for build AJAX Action URL: " + viewId);
		}
		ViewHandler viewHandler = context.getApplication().getViewHandler();
		String actionURL = viewHandler.getActionURL(context, viewId);
		// HACK - check for a Jboss PortletBridge implementation. If present, append DirectLink attribute to url.
		// TODO - how to detect portlet application ?
		if (null != context.getExternalContext().getApplicationMap().get(
				"org.jboss.portletbridge.application.PortletStateHolder")) {
			// Mark Ajax action url as transparent with jsf-portlet bridge.
			actionURL = actionURL
					+ ((actionURL.lastIndexOf('?') > 0) ? "&" : "?")
					+ "javax.portlet.faces.DirectLink=true";

		}
		return context.getExternalContext().encodeActionURL(actionURL);
	}

	/**
	 * @return the commonAjaxParameters
	 */
	public Map<String, Object> getCommonAjaxParameters() {
		return commonAjaxParameters;
	}

	/**
	 * @return the oncomplete
	 */
	public Object getOncomplete() {
		return oncomplete;
	}

	/**
	 * @param oncomplete
	 *            the oncomplete to set
	 */
	public void setOncomplete(Object oncomplete) {
		this.oncomplete = oncomplete;
	}

	private boolean isExtendedSkinningEnabled(ExternalContext context) {
		String userAgent = context.getRequestHeaderMap().get("User-Agent");
		if (userAgent != null) {
			boolean apply = !USER_AGENTS.matcher(userAgent).find();

			if (log.isDebugEnabled()) {
				log.debug("Got User-Agent: " + userAgent);
				log.debug("Applying extended CSS controls styling = " + apply);
			}
			
			return apply;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("User-Agent is null, applying extended CSS controls styling");
			}

			return true;
		}
	}
	
	private String evaluate(FacesContext context, Object parameterValue) {
		if (parameterValue == NULL || parameterValue == null) {
			return null;
		} else if (parameterValue instanceof ValueExpression) {
			ValueExpression expression = (ValueExpression) parameterValue;
			
			return (String) expression.getValue(context.getELContext());
		} else {
			return parameterValue.toString();
		}
	}
	
	private String getInitParameterValue(FacesContext context, String parameterName) {
		
		String key = INIT_PARAMETER_PREFIX + parameterName;
		
		ExternalContext externalContext = context.getExternalContext();
		Map<String, Object> applicationMap = externalContext.getApplicationMap();
		Object mutex = externalContext.getRequest();
		Object parameterValue = null;
		
		synchronized (mutex) {
			parameterValue = applicationMap.get(key);

			if (parameterValue == null) {

				String initParameter = externalContext.getInitParameter(parameterName);
				if (initParameter != null) {
					
					if (ELUtils.isValueReference(initParameter)) {
						Application application = context.getApplication();
						ExpressionFactory expressionFactory = application.getExpressionFactory();
						
						parameterValue = expressionFactory.createValueExpression(context.getELContext(), 
								initParameter,
								String.class);
					} else {
						parameterValue = initParameter;
					}
					
				} else {
					parameterValue = NULL;
				}
				
				applicationMap.put(key, parameterValue);
			}
		}
		
		return evaluate(context, parameterValue);
	}
}
