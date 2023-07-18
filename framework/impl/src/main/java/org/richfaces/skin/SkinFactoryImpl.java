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

package org.richfaces.skin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.el.ReferenceSyntaxException;

import org.ajax4jsf.Messages;
import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.ajax4jsf.util.ELUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of {@link SkinFactory} with building skins from properties
 * files.
 * 
 * @author shura
 * 
 */
public class SkinFactoryImpl extends SkinFactory {

	/**
	 * Name of web application init parameter for current default
	 * {@link javax.faces.render.RenderKit } interaction. by default -
	 * org.exadel.chameleon.RENDERKIT_DEFINITION . TODO Possible Values
	 */
	public static final String RENDER_KIT_PARAMETER = "org.ajax4jsf.RENDERKIT_DEFINITION";

	/**
	 * Resource Uri for properties file with default values of skin parameters.
	 */
	private static final String DEFAULT_SKIN_PROPERTIES_RESOURCE = "META-INF/skins/DEFAULT.skin.properties";

	private static final String DEFAULT_CONFIGURATION_RESOURCE = "META-INF/skins/DEFAULT.configuration.properties";
	/**
	 * Name of default skin . "DEFAULT" in this realisation.
	 */
	private static final String DEFAULT_SKIN_NAME = "DEFAULT";

	/**
	 * Path in jar to pre-defined vendor and custom user-defined skins
	 * definitions. in this realisation "META-INF/skins/" for vendor , "" -
	 * user-defined.
	 */
	private static final String[] SKINS_PATHS = {
			"META-INF/skins/%s.skin.properties", "%s.skin.properties" };
	private static final String[] DEFAULT_SKIN_PATHS = { DEFAULT_SKIN_PROPERTIES_RESOURCE };

	private static final String[] CONFIGURATIONS_PATHS = {
			"META-INF/skins/%s.configuration.properties",
			"%s.configuration.properties" };
	private static final String[] DEFAULT_CONFIGURATION_PATHS = { "META-INF/skins/DEFAULT.configuration.properties" };

	private Map skins = Collections.synchronizedMap(new HashMap());
	private Map baseSkins = Collections.synchronizedMap(new HashMap());
	private Map sourceProperties = Collections.synchronizedMap(new HashMap());

	private Properties defaultSkinProperties = null;
	private String skinName = null;
	private ValueExpression skinBinding = null;
	private String baseSkinName = null;
	private ValueExpression baseSkinBinding = null;
	private static final Log log = LogFactory.getLog(SkinFactoryImpl.class);

	private static final String A4J_BASE_SKIN_PARAMETER = "org.ajax4jsf.BASE_SKIN";

	private static final String A4J_SKIN_PARAMETER = "org.ajax4jsf.SKIN";

	protected Skin getSkinByName(FacesContext facesContext, Object currentSkinOrName, boolean isBase) {
		if (null == currentSkinOrName) {
			throw new SkinNotFoundException(Messages
					.getMessage(Messages.NULL_SKIN_NAME_ERROR));
		}
		// user binding return skin instance.
		if (currentSkinOrName instanceof Skin) {
			return (Skin) currentSkinOrName;
		}
		String currentSkinName = currentSkinOrName.toString();
		
		Map skinsMap = (isBase ? baseSkins : skins);
		Skin currentSkin = (Skin) skinsMap.get(currentSkinName);
		// LAZY creation for skins, since, in case of EL expressions
		// for skin name, we don't can know all names of existing skins.
		if (currentSkin == null) {
			if (log.isDebugEnabled()) {
				log.debug(Messages.getMessage(Messages.CREATE_SKIN_INFO,
						currentSkinName));
			}
			currentSkin = buildSkin(facesContext, currentSkinName, isBase);
			skinsMap.put(currentSkinName, currentSkin);
		}

		return currentSkin;
	}

	public Skin getDefaultSkin(FacesContext context) {
		return getSkinByName(context, DEFAULT_SKIN_NAME, false);
	}

	public Skin getSkin(FacesContext context) {
		// TODO - cache skin for current thread ? or for current Faces Lifecycle
		// Phase ?
		Object currentSkinOrName = getSkinOrName(context, false);
		return getSkinByName(context, currentSkinOrName, false);
	}

	public Skin getBaseSkin(FacesContext context) {
		Object currentSkinOrName = getSkinOrName(context, true);
		return getSkinByName(context, currentSkinOrName, true);
	}

	protected Properties getDefaultSkinProperties() {
		if (defaultSkinProperties == null) {
			defaultSkinProperties = loadDefaultProperties(DEFAULT_SKIN_PROPERTIES_RESOURCE);
		}
		return defaultSkinProperties;
	}

	/**
	 * 
	 */
	private Properties loadDefaultProperties(String path) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties defaultProperties = new Properties();
		try {
			Enumeration properties = loader.getResources(path);
			while (properties.hasMoreElements()) {
				URL url = (URL) properties.nextElement();
				InputStream propertyStream = null;
				try {
					propertyStream = URLToStreamHelper.urlToStream(url);
					defaultProperties.load(propertyStream);
				} catch (IOException e) {
					// DO Nothing...
					log.info(Messages.getMessage(
							Messages.DEFAULT_PROPERTIES_NOT_FOUND_ERROR, path),
							e);
					continue;
				} finally {
					if (null != propertyStream) {
						propertyStream.close();
					}
				}
			}
		} catch (IOException e) {
			log.info(Messages.getMessage(
					Messages.DEFAULT_PROPERTIES_NOT_FOUND_ERROR, path), e);
		}
		return defaultProperties;
	}

	/**
	 * Calculate name for current skin. For EL init parameter store value
	 * binding for speed calculations.
	 * 
	 * @param context
	 * @param useBase
	 * @return name of currens skin from init parameter ( "DEFAULT" if no
	 *         parameter ) or {@link Skin } as result of evaluation EL
	 *         expression.
	 */
	protected Object getSkinOrName(FacesContext context, boolean useBase) {
		// Detect skin name
		ValueExpression binding;
		String skin;

		synchronized (this) {
			if (useBase) {
				binding = baseSkinBinding;
				skin = baseSkinName;
			} else {
				binding = skinBinding;
				skin = skinName;
			}

			if (binding == null && skin == null) {
				String currentSkinName = context.getExternalContext()
						.getInitParameter(
								useBase ? BASE_SKIN_PARAMETER : SKIN_PARAMETER);
				if (null == currentSkinName) {
					// Check for a old ( deprecated ) parameter name.
					currentSkinName = context.getExternalContext()
							.getInitParameter(
									useBase ? A4J_BASE_SKIN_PARAMETER
											: A4J_SKIN_PARAMETER);
					if (null != currentSkinName) {
						log.warn("Init parameter for a skin name changed to "+ (useBase ? BASE_SKIN_PARAMETER
											: SKIN_PARAMETER));
					}

				}
				if (currentSkinName == null) {
					// not set - usr default.
					return DEFAULT_SKIN_NAME;
				}
				if (ELUtils.isValueReference(currentSkinName)) {
					// For EL expression as skin name
					binding = context.getApplication().getExpressionFactory().
						createValueExpression(context.getELContext(), 
							currentSkinName, Object.class);
				} else {
					skin = currentSkinName;
				}
			}

			if (useBase) {
				baseSkinBinding = binding;
				baseSkinName = skin;
			} else {
				skinBinding = binding;
				skinName = skin;
			}

			// }
		}
		if (binding != null) {
			return binding.getValue(context.getELContext());
		} else {
			return skin;
		}
	}

	private void processProperties(FacesContext context, Map<Object, Object> properties) {
	    ELContext elContext = context.getELContext();
	    // replace all EL-expressions by prepared ValueBinding ?
	    ApplicationFactory factory = (ApplicationFactory) FactoryFinder
	    .getFactory(FactoryFinder.APPLICATION_FACTORY);
	    Application app = factory.getApplication();
	    
	    for (Entry<Object, Object> entry : properties.entrySet()) {
		Object propertyObject = entry.getValue();
		if (propertyObject instanceof String) {
		    String property = (String) propertyObject;
		    if (ELUtils.isValueReference(property)) {
			ExpressionFactory expressionFactory = app.getExpressionFactory();
			entry.setValue(expressionFactory.createValueExpression(elContext, property, Object.class));
		    } else {
			entry.setValue(property);
		    }
		}
	    }
	}
	
	/**
	 * Factory method for build skin from properties files. for given skin name,
	 * search in classpath all resources with name 'name'.skin.properties and
	 * append in content to default properties. First, get it from
	 * META-INF/skins/ , next - from root package. for any place search order
	 * determined by {@link java.lang.ClassLoader } realisation.
	 * @param name
	 *            name for builded skin.
	 * @param defaultProperties
	 * 
	 * @return skin instance for current name
	 * @throws SkinNotFoundException -
	 *             if no skin properies found for name.
	 */
	protected Skin buildSkin(FacesContext context, String name, boolean isBase)
			throws SkinNotFoundException {
	
	    Map<Object, Object> skinParams = (Map<Object, Object>) sourceProperties.get(name);
	    if (skinParams == null) {
		skinParams = loadProperties(name, SKINS_PATHS);
		processProperties(context, skinParams);
		skinParams = Collections.unmodifiableMap(skinParams);
		sourceProperties.put(name, skinParams);
	    }
	    
	    BasicSkinImpl skinImpl;
	    if (DEFAULT_SKIN_NAME.equals(name)) {
		skinImpl = new DefaultSkinImpl(skinParams);
	    } else if (isBase) {
		skinImpl = new BaseSkinImpl(skinParams, this);
	    } else {
		skinImpl = new SkinImpl(skinParams, this);
	    }

	    return skinImpl;
	}

	/**
	 * @param name
	 * @param defaultProperties
	 * @return
	 * @throws SkinNotFoundException
	 * @throws FacesException
	 * @throws ReferenceSyntaxException
	 */
	protected Map loadProperties(String name, String[] paths) throws SkinNotFoundException, FacesException,
			ReferenceSyntaxException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		// Get properties for concrete skin.
		Properties skinProperties = new Properties();
		int loadedPropertiesCount = 0;
		for (int i = 0; i < paths.length; i++) {
			try {
				String skinPropertiesLocation = paths[i].replaceAll("%s", name);
				Enumeration properties = loader
						.getResources(skinPropertiesLocation);
				while (properties.hasMoreElements()) {
					URL url = (URL) properties.nextElement();
					InputStream propertyStream = null;
					try {
						propertyStream = URLToStreamHelper.urlToStream(url);
						skinProperties.load(propertyStream);
						loadedPropertiesCount++;
					} catch (IOException e) {
						log.warn(Messages
								.getMessage(Messages.SKIN_PROPERTIES_IO_ERROR),
								e);
						continue;
					} finally {
						if (null != propertyStream) {
							propertyStream.close();
						}
					}
				}
			} catch (IOException e) {
				// Do nothing - we can only log error, and continue to load next
				// property.
				if (log.isInfoEnabled()) {
					log.info(Messages
							.getMessage(Messages.SKIN_PROPERTIES_IO_ERROR), e);
				}
			}
		}
		// TODO - read skin from property file in WEB-INF directory.
		if (loadedPropertiesCount == 0) {
			throw new SkinNotFoundException(Messages.getMessage(
					Messages.SKIN_NOT_FOUND_ERROR, name));
		}
		return skinProperties;
	}

}
