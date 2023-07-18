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

package org.ajax4jsf.application;

import java.io.IOException;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.Messages;
import org.ajax4jsf.component.AjaxViewRoot;
import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.context.ViewIdHolder;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
import org.ajax4jsf.resource.InternetResource;
import org.ajax4jsf.resource.InternetResourceBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.5 $ $Date: 2007/02/08 15:02:04 $
 * 
 */
public class AjaxViewHandler extends ViewHandlerWrapper {

	public static final String STATE_MARKER_KEY = "org.ajax4jsf.view.state";

	public static final String SERIALIZED_STATE_KEY = "org.ajax4jsf.view.serializedstate";

	private static final Log _log = LogFactory.getLog(AjaxViewHandler.class);

	public static final String VIEW_EXPIRED = "org.ajax4jsf.view.EXPIRED";

	public static final String VIEW_ID_KEY = "org.ajax4jsf.VIEW_ID";

	/**
	 * @param parent
	 */
	public AjaxViewHandler(ViewHandler parent) {
		super(parent);
		if (_log.isDebugEnabled()) {
			_log.debug("Create instance of Ajax ViewHandler");
		}
	}

	/*
	 * (non-Javadoc) For creating ViewRoot by Application Instead of new . Not
	 * nessesary for MyFaces ( simple copy from it ) or RI 1.2
	 * 
	 * @see javax.faces.application.ViewHandler#createView(javax.faces.context.FacesContext,
	 *      java.lang.String)
	 */
	public UIViewRoot createView(FacesContext facesContext, String viewId) {
		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(facesContext);
		// Check for simple keep new ViewId in navigation cases.
		ViewIdHolder viewIdHolder = ajaxContext.getViewIdHolder();
		UIViewRoot riRoot;
		if (null != viewIdHolder && viewIdHolder.skipNavigation(viewId)) {
			viewIdHolder.setViewId(viewId);
			riRoot = facesContext.getViewRoot();
		} else {
			riRoot = super.createView(facesContext, viewId);
			// Reset ajax request status for a navigation case.
			if(null != facesContext.getViewRoot()){
				ajaxContext.setAjaxRequest(false);
			}
		}
		return riRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.framework.ViewHandlerWrapper#writeState(javax.faces.context.FacesContext)
	 */
	public void writeState(FacesContext context) throws IOException {
		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
		if (ajaxContext.isAjaxRequest() && ajaxContext.isSelfRender()) {
			// We need own
			// state marker for
			// self-rendered regions only.
			// write marker html element - input field. Will be parsed in filter
			// and
			// replaced with real state.
			ResponseWriter writer = context.getResponseWriter();
			writer.startElement(HTML.SPAN_ELEM, null);
			writer.writeAttribute(HTML.id_ATTRIBUTE, STATE_MARKER_KEY, null);
			writer.writeAttribute(HTML.NAME_ATTRIBUTE, STATE_MARKER_KEY, null);
//			// writer.writeAttribute("style", "display:none;", null);
//			if (!ajaxContext.isSelfRender()) {
//				super.writeState(context);
//			}
			writer.endElement(HTML.SPAN_ELEM);
		} else {
			super.writeState(context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.framework.ViewHandlerWrapper#restoreView(javax.faces.context.FacesContext,
	 *      java.lang.String)
	 */
//	public UIViewRoot restoreView(FacesContext context, String viewId) {
//		UIViewRoot viewRoot = super.restoreView(context, viewId);
//		ExternalContext externalContext = context.getExternalContext();
//		Map<String, Object> requestMap = externalContext.getRequestMap();
//		if (null == viewRoot) {
//			requestMap.put(VIEW_EXPIRED, Messages
//					.getMessage(Messages.AJAX_VIEW_EXPIRED));
//			if (_log.isDebugEnabled()) {
//				_log
//						.debug("Detect session expiration in AJAX request - view don't restored for a viewId "
//								+ viewId);
//			}
//		}
//		return viewRoot;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.framework.ViewHandlerWrapper#getResourceURL(javax.faces.context.FacesContext,
	 *      java.lang.String)
	 */
	public String getResourceURL(FacesContext context, String url) {
		String resourceURL;
		if (url.startsWith(InternetResource.RESOURCE_URI_PREFIX)) {
			InternetResource resource = InternetResourceBuilder
					.getInstance()
					.createResource(
							null,
							url
									.substring(InternetResource.RESOURCE_URI_PREFIX_LENGTH));
			resourceURL = resource.getUri(context, null);
		} else {
			resourceURL = super.getResourceURL(context, url);
		}
		return resourceURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.framework.ViewHandlerWrapper#renderView(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIViewRoot)
	 */
	public void renderView(FacesContext context, UIViewRoot root)
			throws IOException, FacesException {
		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
		// Broadcast Ajax events before rendering, to setup areas to update.
		if (root instanceof AjaxViewRoot) {
			AjaxViewRoot ajaxRoot = (AjaxViewRoot) root;
			// broadcast ajax events before render response.
			if (ajaxContext.isAjaxRequest()) {
				processAjaxEvents(context, ajaxRoot);
				if(ajaxContext.isSelfRender()){
					// Render view directly.
					ajaxContext.renderAjax(context);
				}
			}
			if (!context.getResponseComplete()) {
				super.renderView(context, root);
			}
		} else {
			super.renderView(context, root);
		}
		ajaxContext.processHeadResources(context);
	}

	/**
	 * @param context
	 * @param ajaxRoot
	 */
	private void processAjaxEvents(FacesContext context, AjaxViewRoot ajaxRoot) {
		// First - process reRender from active components.
		if (_log.isDebugEnabled()) {
			_log.debug(Messages.getMessage(Messages.PROCESS_AJAX_EVENTS_INFO));
		}
		ajaxRoot.broadcastAjaxEvents(context);
	}

}
