package org.ajax4jsf.application;

public interface StateHolder {

	public Object[] getState(String viewId, String sequence);

	public void saveState(String viewId, String sequence, Object state[]);

}