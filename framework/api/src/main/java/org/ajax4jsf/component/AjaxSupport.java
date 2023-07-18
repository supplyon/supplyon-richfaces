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

package org.ajax4jsf.component;

import javax.faces.component.UIComponent;

/**
 * Version of {@link org.ajax4jsf.component.AjaxComponent} for append
 * Ajax functions for non-ajax components
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:57:34 $
 *
 */
public interface AjaxSupport  {

    /**
     * @return JavaScript eventString. Rebuild on every call, since
     * can be in loop ( as in dataTable ) with different parameters.
     */
    public String getEventString();
    /**
     * setter method for property
     * @param new value of Name of event property of parent component for build JavaScript AJAX.Submit call to set
     */
    public abstract void setEvent(String event);

    /**
     * @return value or result of valueBinding of Name of event property of parent component for build JavaScript AJAX.Submit call
     */
    public abstract String getEvent();


    /**
	 * Getter for property If true, disable default action for target event ( encode 'return false' to JavaScript ), implementation will be generated by
	 * componnents-generator
	 * @return property value
	 */
	public abstract boolean isDisableDefault();

	/**
	 * Setter for property If true, disable default action for target event ( encode 'return false' to JavaScript ), implementation will be generated by
	 * componnents-generator
	 * @param newvalue - new property value
	 */
	public abstract void setDisableDefault(boolean newvalue);
	
	/**
	 * Set property for JavaScrept event generated by this component.
	 * @param parent
	 */
	public void setParentProperties(UIComponent parent);
}
