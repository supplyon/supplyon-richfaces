package org.ajax4jsf.application;

import javax.faces.component.UIComponent;

public interface ComponentsLoader {

    public abstract UIComponent createComponent(String type);

}