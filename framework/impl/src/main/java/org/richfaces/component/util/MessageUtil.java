/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
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

package org.richfaces.component.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author Nick - mailto:nbelaevski@exadel.com
 * created 06.02.2007
 * 
 */
public class MessageUtil {
	private static final boolean IS_12;
	
	static {
		boolean is12;
		try {
			Application.class.getMethod("getExpressionFactory", null);
			is12 = true;
		} catch (NoSuchMethodException e) {
			is12 = false;
		}
		
		IS_12 = is12;
	}
	
	
	public static Object getLabel(FacesContext context, UIComponent component) {
		Object o = null;
		if (IS_12) {
			o = component.getAttributes().get("label");
			if (o == null || (o instanceof String && ((String) o).length() == 0)) {
				ValueBinding ex = component.getValueBinding("label");
				if (ex != null) {
					o = ex.getValue(context);
				}
			}
		}

		if (o == null) {
			o = component.getClientId(context);
		}
		
		return o;
	}
	
	private static final ResourceBundle getResourceBundle(String baseName, Locale locale, ClassLoader loader) {
		if (loader != null) {
			return ResourceBundle.getBundle(baseName, locale, loader);
		} else {
			return ResourceBundle.getBundle(baseName, locale);
		}
	}
	
	private static final FacesMessage getMessage(FacesContext context, String messageId, 
			Object[] parameters, Locale locale) {
		String summary = null;
		String detail = null;
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		if (context != null) {
			Application application = context.getApplication();
			if (application != null) {
				String messageBundleName = application.getMessageBundle();
	
				if (messageBundleName != null) {
					ResourceBundle bundle = getResourceBundle(messageBundleName, locale, loader);
					if (bundle != null) {
						try {
							summary = bundle.getString(messageId);
							detail = bundle.getString(messageId + "_detail");
						} catch (MissingResourceException e) {
							//do nothing
						}
					}
				}
			}
		}

		if (summary == null) {
			ResourceBundle bundle = getResourceBundle(FacesMessage.FACES_MESSAGES, locale, loader);
			try {
				summary = bundle.getString(messageId);
				
				if (summary == null) {
					return null;
				}

				detail = bundle.getString(messageId + "_detail");
			} catch (MissingResourceException e) {
				//do nothing
			}
		}
		
		String formattedSummary = MessageFormat.format(summary, parameters);
		String formattedDetail = null;
		if (detail != null) {
			formattedDetail = MessageFormat.format(detail, parameters);
		}
		
		return new FacesMessage(formattedSummary, formattedDetail);
	}
	
	public static final FacesMessage getMessage(FacesContext context, String messageId, 
			Object[] parameters) {

		Locale locale;
		FacesMessage result = null;
		
		if (context != null) {
			UIViewRoot viewRoot = context.getViewRoot();
			if (viewRoot != null) {
				locale = viewRoot.getLocale();
			
				if (locale != null) {
					result = getMessage(context, messageId, parameters, locale);
				}
			}
		}
		
		if (result == null) {
			locale = Locale.getDefault();
			result = getMessage(context, messageId, parameters, locale);
		}

		return result;
	}
}
