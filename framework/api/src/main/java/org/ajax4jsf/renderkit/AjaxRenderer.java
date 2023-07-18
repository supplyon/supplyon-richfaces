package org.ajax4jsf.renderkit;

import java.io.IOException;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public interface AjaxRenderer {

	/**
	 * Iterate over all childs of components. If component id contains in list ,
	 * or, if list is empty, compotents is submitted form - render it. TODO -
	 * Instead of calculate full path for every component, build current Path
	 * for componet and send as parameter.
	 * 
	 * @param context -
	 *            current context
	 * @param component -
	 *            curent faces component.
	 * @param ids -
	 *            list of Id to render.
	 * @throws IOException
	 */
	public void encodeAjaxChildren(FacesContext context, UIComponent component,
			String path, Set<String> ids, Set<String> renderedAreas) throws IOException;

	/**
	 * @param context
	 * @param component
	 * @param currentPath
	 * @param ids
	 * @param renderedAreas
	 * @throws IOException
	 */
	public void encodeAjaxComponent(FacesContext context,
			UIComponent component, String currentPath, Set<String> ids,
			Set<String> renderedAreas) throws IOException;

}