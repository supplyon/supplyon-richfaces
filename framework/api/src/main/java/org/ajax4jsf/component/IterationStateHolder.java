/**
 * 
 */
package org.ajax4jsf.component;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIData;

/**
 * In the original {@link UIData} component, only state for a {@link EditableValueHolder} component saved for an iteration.
 * In the Richfaces, we also save state for a components implemented this interface.
 * @author asmirnov
 *
 */
public interface IterationStateHolder {
	
	/**
	 * Get component state for a current iteration.
	 * @return request-scope component state. Details are subject for a component implementation 
	 */
	public Object getIterationState();
	
	/**
	 * Restore component state from previsious saved value.
	 * @param state request-scope component state. Details are subject for a component implementation
	 */
	public void setIterationState( Object state);
	
}
