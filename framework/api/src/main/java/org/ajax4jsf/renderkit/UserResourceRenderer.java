package org.ajax4jsf.renderkit;

import java.util.LinkedHashSet;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Marker interface for a user-defined resources for a HTML HEAD tag.
 * There resourses must be loaded AFTER all other components.
 * @author asmirnov
 *
 */
public interface UserResourceRenderer {

	/**
	 * Return set of strings with URI's of nessesary scripts.
	 * Use linked set to preserve insertion order
	 * @param context - current faces context.
	 * @param component TODO
	 * @return - set of URI's or null
	 */
	public LinkedHashSet<String> getHeaderScripts(FacesContext context, UIComponent component);

	/**
	 * Return set of strings with URI's of nessesary CSS styles.
	 * Use linked set to preserve insertion order.
	 * @param context - current faces context.
	 * @param component TODO
	 * @return - set of URI's or null
	 */
	public LinkedHashSet<String> getHeaderStyles(FacesContext context, UIComponent component);

}
