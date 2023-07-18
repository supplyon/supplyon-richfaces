/**
 * 
 */
package org.ajax4jsf.application;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.ajax4jsf.context.ContextInitParameters;
import org.ajax4jsf.util.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author asmirnov
 * 
 */
public class AjaxStateHolder implements Serializable, StateHolder {

	private static final Log _log = LogFactory.getLog(AjaxStateHolder.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 6414488517358423537L;
	private static final String STATE_HOLDER = AjaxStateHolder.class.getName();

	private final LRUMap<String,LRUMap<String, Object[]>> views;

	private final int numberOfViews;

	private AjaxStateHolder(int capacity, int numberOfViews) {
		views = new LRUMap<String,LRUMap<String, Object[]>>(capacity);
		this.numberOfViews = numberOfViews;
	}

	public static StateHolder getInstance(FacesContext context) {
		if (null == context) {
			throw new NullPointerException(
					"FacesContext parameter for get view states object is null");
		}
		ExternalContext externalContext = context.getExternalContext();
		Object session = externalContext.getSession(true);
		Map<String,Object> sessionMap = externalContext.getSessionMap();
		if (_log.isDebugEnabled()) {
			_log.debug("Request for a view states holder instance");
		}
		StateHolder instance = null;
		synchronized (session) {
			instance = (StateHolder) sessionMap.get(STATE_HOLDER);
			if (null == instance) {
				// Create and store in session new state holder.
				int numbersOfViewsInSession = ContextInitParameters
						.getNumbersOfViewsInSession(context);
				int numbersOfLogicalViews = ContextInitParameters
						.getNumbersOfLogicalViews(context);
				if (_log.isDebugEnabled()) {
					_log
							.debug("No AjaxStateHolder instance in session, create new for hold "
									+ numbersOfViewsInSession
									+ " viewId and "
									+ numbersOfLogicalViews
									+ " logical views for each");
				}
				instance = new AjaxStateHolder(numbersOfViewsInSession,
						numbersOfLogicalViews);
				sessionMap.put(STATE_HOLDER, instance);
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.application.StateHolder#getState(java.lang.String, java.lang.Object)
	 */
	public Object[] getState(String viewId, String sequence) {
		if (null == viewId) {
			throw new NullPointerException(
					"viewId parameter for get saved view state is null");
		}
		Object state[] = null;
		synchronized (views) {
			LRUMap<String,Object[]> viewVersions = views.get(viewId);
			if (null != viewVersions) {
				if (null != sequence) {
					state = viewVersions.get(sequence);
				}
				if (null == state) {
					if (_log.isDebugEnabled()) {
						_log.debug("No saved view state for sequence "+sequence);
					}
//					state = viewVersions.getMostRecent();
				}
			} else if (_log.isDebugEnabled()) {
				_log.debug("No saved view states for viewId "+viewId);
			}
		}
		return state;
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.application.StateHolder#saveState(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void saveState(String viewId, String sequence, Object[] state) {
		if (null == viewId) {
			throw new NullPointerException(
					"viewId parameter for  save view state is null");
		}
		if (null == sequence) {
			throw new NullPointerException(
					"sequence parameter for save view state is null");
		}
		if (null != state) {
			if (_log.isDebugEnabled()) {
				_log.debug("Save new viewState in session for viewId "+viewId+" and sequence "+sequence);
			}
			synchronized (views) {
				LRUMap<String,Object[]> viewVersions = views.get(viewId);
				if (null == viewVersions) {
					// TODO - make size parameter configurable
					viewVersions = new LRUMap<String,Object[]>(this.numberOfViews);
					views.put(viewId, viewVersions);
				}
				viewVersions.put(sequence, state);
			}

		}
	}
}
