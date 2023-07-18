package org.ajax4jsf.event;

import java.util.EventListener;
import java.util.EventObject;

public interface PushEventListener extends EventListener {
    
    public void onEvent(EventObject event);

}
