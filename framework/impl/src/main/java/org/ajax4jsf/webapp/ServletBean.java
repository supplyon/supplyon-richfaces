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

package org.ajax4jsf.webapp;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:58:59 $
 *
 */
public class ServletBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7485005767985264890L;
	private String _servletName;
	private String _servletClass;
	private String _displayName;
	private String _description;
	private Set<String> _servletMappings = new HashSet<String>();
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return _description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		_description = description;
	}
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return _displayName;
	}
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}
	/**
	 * @return Returns the servletClass.
	 */
	public String getServletClass() {
		return _servletClass;
	}
	/**
	 * @param servletClass The servletClass to set.
	 */
	public void setServletClass(String servletClass) {
		_servletClass = servletClass;
	}
	/**
	 * @return Returns the servletName.
	 */
	public String getServletName() {
		return _servletName;
	}
	/**
	 * @param servletName The servletName to set.
	 */
	public void setServletName(String servletName) {
		_servletName = servletName.trim();
	}
	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set<String> getMappings() {
		return _servletMappings;
	}
	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	void addMapping(String mapping) {
		_servletMappings.add(mapping);
	}


}
