package org.ajax4jsf.webapp;

import java.io.Serializable;
import java.util.EventObject;

import org.ajax4jsf.event.PushEventListener;

public class PushEventsCounter implements PushEventListener,Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 4060284352186710009L;
    private volatile boolean performed = false;

    public void onEvent(EventObject event) {
	performed = true;
    }

    /**
     * @return the performed
     */
    public boolean isPerformed() {
        return performed;
    }

    /**
     */
    public void processed() {
        this.performed = false;
    }

    
}
