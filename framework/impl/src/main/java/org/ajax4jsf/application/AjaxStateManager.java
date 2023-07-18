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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.FactoryFinder;
import javax.faces.application.StateManager;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;

import org.ajax4jsf.context.AjaxContext;
import org.ajax4jsf.model.KeepAlive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author shura
 * 
 */
public class AjaxStateManager extends StateManager {

	private final class SeamStateManagerWrapper extends StateManager {
		protected Object getComponentStateToSave(FacesContext arg0) {
			// do nothing
			return null;
		}

		protected Object getTreeStructureToSave(FacesContext arg0) {
			// do nothing
			return null;
		}

		protected void restoreComponentState(FacesContext arg0,
				UIViewRoot arg1, String arg2) {
			// do nothing

		}

		protected UIViewRoot restoreTreeStructure(FacesContext arg0,
				String arg1, String arg2) {
			// do nothing
			return null;
		}

		public UIViewRoot restoreView(FacesContext arg0, String arg1,
				String arg2) {
			// do nothing
			return null;
		}

		@SuppressWarnings("deprecation")
		public SerializedView saveSerializedView(FacesContext arg0) {
			// delegate to enclosed class method.
			return buildSerializedView(arg0);
		}

		@SuppressWarnings("deprecation")
		public void writeState(FacesContext arg0, SerializedView arg1)
				throws IOException {
			// do nothing
		}
	}

	private static final Class<StateManager> STATE_MANAGER_ARGUMENTS = StateManager.class;

	public static final int DEFAULT_NUMBER_OF_VIEWS = 16;


	public static final String VIEW_SEQUENCE = AjaxStateManager.class
			.getName()
			+ ".VIEW_SEQUENCE";

	private final StateManager parent;

	private StateManager seamStateManager;

	private final ComponentsLoader componentLoader;

	private static final Log _log = LogFactory.getLog(AjaxStateManager.class);

	public static final String VIEW_SEQUENCE_ATTRIBUTE = AjaxStateManager.class.getName()+".view_sequence";

	/**
	 * @param parent
	 */
	public AjaxStateManager(StateManager parent) {
		super();
		this.parent = parent;
		componentLoader = new ComponentsLoaderImpl();
		// HACK - Seam perform significant operations before save tree state.
		// Try to create it instance by reflection,
		// to call in real state saving operations.
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (null == classLoader) {
			classLoader = AjaxStateManager.class.getClassLoader();
		}
		try {
			Class<? extends StateManager> seamStateManagerClass = classLoader
					.loadClass("org.jboss.seam.jsf.SeamStateManager").asSubclass(StateManager.class);
			Constructor<? extends StateManager> constructor = seamStateManagerClass
					.getConstructor(STATE_MANAGER_ARGUMENTS);
			seamStateManager = constructor
					.newInstance(new Object[] { new SeamStateManagerWrapper() });
			if (_log.isDebugEnabled()) {
				_log.debug("Create instance of the SeamStateManager");
			}
		} catch (Exception e) {
			seamStateManager = null;
			if (_log.isDebugEnabled()) {
				_log.debug("SeamStateManager is not present");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#getComponentStateToSave(javax.faces.context.FacesContext)
	 */
	protected Object getComponentStateToSave(FacesContext context) {
		Object treeState = context.getViewRoot().processSaveState(context);
		Object state[] = { treeState, getAdditionalState(context) };
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#getTreeStructureToSave(javax.faces.context.FacesContext)
	 */
	protected Object getTreeStructureToSave(FacesContext context) {
		TreeStructureNode treeStructure = new TreeStructureNode();
		treeStructure.apply(context, context.getViewRoot(), new HashSet<String>());
		return treeStructure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#restoreComponentState(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIViewRoot, java.lang.String)
	 */
	protected void restoreComponentState(FacesContext context,
			UIViewRoot viewRoot, String renderKitId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#restoreTreeStructure(javax.faces.context.FacesContext,
	 *      java.lang.String, java.lang.String)
	 */
	protected UIViewRoot restoreTreeStructure(FacesContext context,
			String viewId, String renderKitId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#writeState(javax.faces.context.FacesContext,
	 *      javax.faces.application.StateManager.SerializedView)
	 */
	@SuppressWarnings("deprecation")
	public void writeState(FacesContext context, SerializedView state)
			throws IOException {
		parent.writeState(context, state);
		if (_log.isDebugEnabled()) {
			_log.debug("Write view state to the response");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.application.StateManager#restoreView(javax.faces.context.FacesContext,
	 *      java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("deprecation")
	public UIViewRoot restoreView(FacesContext context, String viewId,
			String renderKitId) {
		UIViewRoot viewRoot = null;
		ResponseStateManager responseStateManager = getRenderKit(context,
				renderKitId).getResponseStateManager();
		TreeStructureNode treeStructure = null;
		Object[] state = null;
		if (isSavingStateInClient(context)) {
			treeStructure = (TreeStructureNode) responseStateManager
					.getTreeStructureToRestore(context, viewId);
			// viewRoot = parent.restoreView(context, viewId, renderKitId);
			state = (Object[]) responseStateManager
					.getComponentStateToRestore(context);
		} else {
			Object[] serializedView = restoreStateFromSession(context, viewId,
					renderKitId);
			if (null != serializedView) {
				treeStructure = (TreeStructureNode) serializedView[0];
				state = (Object[]) serializedView[1];
			}
		}
		if (null != treeStructure) {
			viewRoot = (UIViewRoot) treeStructure.restore(componentLoader);
			if (null != viewRoot && null != state) {
				viewRoot.processRestoreState(context, state[0]);
				restoreAdditionalState(context, state[1]);
			}
		}
		return viewRoot;

	}

	protected Object[] restoreStateFromSession(FacesContext context,
			String viewId, String renderKitId) {
		String id = restoreLogicalViewId(context, viewId, renderKitId);
		StateHolder stateHolder = getStateHolder(context);
		Object[] restoredState = stateHolder.getState(viewId, id);
		return restoredState;
	}

	@SuppressWarnings("deprecation")
	public SerializedView saveSerializedView(FacesContext context) {
		if (null == seamStateManager) {
			return buildSerializedView(context);
		} else {
			// Delegate save method to seam State Manager.
			return seamStateManager.saveSerializedView(context);
		}
	}

	/**
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected SerializedView buildSerializedView(FacesContext context) {
		SerializedView serializedView = null;
		UIViewRoot viewRoot = context.getViewRoot();
		if (null !=viewRoot && !viewRoot.isTransient()) {
			TreeStructureNode treeStructure = (TreeStructureNode) getTreeStructureToSave(context);
			Object state = getComponentStateToSave(context);
			if (isSavingStateInClient(context)) {
				serializedView = new SerializedView(treeStructure, state);
			} else {
				serializedView = saveStateInSession(context, treeStructure,
						state);
			}

		}
		return serializedView;
	}

	/**
	 * @param context
	 * @param treeStructure
	 * @param state
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected SerializedView saveStateInSession(FacesContext context,
			Object treeStructure, Object state) {
		SerializedView serializedView;
		UIViewRoot viewRoot = context.getViewRoot();
		StateHolder stateHolder = getStateHolder(context);
		String id = getLogicalViewId(context);
		stateHolder.saveState(viewRoot.getViewId(), id, new Object[] {
				treeStructure, state });
		serializedView = new SerializedView(id, null);
		return serializedView;
	}

	/**
	 * @param context
	 * @return
	 */
	protected StateHolder getStateHolder(FacesContext context) {
		return AjaxStateHolder.getInstance(context);
	}

	protected Object getAdditionalState(FacesContext context) {
		Map<String, Object> keepAliveBeans=new HashMap<String, Object>();
		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
		// Save all objects form request map wich marked by @KeepAlive annotations
		for (Entry<String, Object> requestEntry : requestMap.entrySet()) {
			Object bean = requestEntry.getValue();
			// check value for a NULL - http://jira.jboss.com/jira/browse/RF-3576
			if (null != bean && bean.getClass().isAnnotationPresent(KeepAlive.class)) {
				keepAliveBeans.put(requestEntry.getKey(), bean);
			}
		}
		if(keepAliveBeans.size()>0){
			return UIComponentBase.saveAttachedState(context, keepAliveBeans);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void restoreAdditionalState(FacesContext context, Object state) {
		if(null != state){
			// Append all saved beans to the request map.
			Map beansMap = (Map) UIComponentBase.restoreAttachedState(context, state);
			Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
			for (Object key : beansMap.keySet()) {
				requestMap.put((String) key, beansMap.get(key));
			}
		}
	}

	/**
	 * Restore logical view id from request.
	 * @param context
	 * @param viewId
	 * @param renderKitId
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected String restoreLogicalViewId(FacesContext context, String viewId,
			String renderKitId) {
		String id = (String) getRenderKit(context, renderKitId)
				.getResponseStateManager().getTreeStructureToRestore(context,
						viewId);
		if (null != id) {
			context.getExternalContext().getRequestMap().put(VIEW_SEQUENCE, id);
		}
		return id;
	}

	/**
	 * Return logical Id for current request view state. For a faces requests, generate sequence numbers.
	 * For a ajax request, attempt to re-use id from request submit. 
	 * @param context
	 * @return
	 */
	protected String getLogicalViewId(FacesContext context) {
		AjaxContext ajaxContext = AjaxContext.getCurrentInstance(context);
		ExternalContext externalContext = context.getExternalContext();
		if (ajaxContext.isAjaxRequest()) {
			Object id = externalContext.getRequestMap().get(
					VIEW_SEQUENCE);
			if (null != id) {
				return id.toString();
			}
		}
		// Store sequence in session, to avoyd claster configuration problem
		// see https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=662
		Object session = externalContext.getSession(true);
		int viewSequence;
		synchronized (session) {
			Map<String, Object> sessionMap = externalContext.getSessionMap();
			Integer sequence = (Integer) sessionMap.get(VIEW_SEQUENCE_ATTRIBUTE);
			if(null != sequence){
				viewSequence = sequence.intValue();
			} else {
				viewSequence = 0;
			}
			if (viewSequence++ == Character.MAX_VALUE) {
				viewSequence = 0;
			}
			sessionMap.put(VIEW_SEQUENCE_ATTRIBUTE, new Integer(viewSequence));
		}
		return UIViewRoot.UNIQUE_ID_PREFIX + ((int) viewSequence);
	}


	protected RenderKit getRenderKit(FacesContext context, String renderKitId) {
		RenderKit renderKit = context.getRenderKit();
		if (null == renderKit) {
			RenderKitFactory factory = (RenderKitFactory) FactoryFinder
					.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
			renderKit = factory.getRenderKit(context, renderKitId);
		}
		return renderKit;
	}

}
