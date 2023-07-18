/**
 * 
 */
package org.ajax4jsf.context;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;

import org.ajax4jsf.renderkit.HeaderResourceProducer;
import org.ajax4jsf.renderkit.UserResourceRenderer;

/**
 * @author asmirnov
 * 
 */
public class ViewResources {

	LinkedHashSet<String> scripts = new LinkedHashSet<String>();
	LinkedHashSet<String> styles = new LinkedHashSet<String>();
	LinkedHashSet<String> userScripts = new LinkedHashSet<String>();
	LinkedHashSet<String> userStyles = new LinkedHashSet<String>();
	boolean processStyles = true;
	boolean processScripts = true;
	RenderKit renderKit = null;

	public void collect(FacesContext context) {
		UIViewRoot root = context.getViewRoot();
		RenderKitFactory rkFactory = (RenderKitFactory) FactoryFinder
				.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
		renderKit = rkFactory.getRenderKit(context, context
				.getViewRoot().getRenderKitId());
		processHeadResources(context, root);
		//styles.addAll(userStyles);
		scripts.addAll(userScripts);
	}

	/**
	 * Append nessesary scripts and styles from component ( if renderer
	 * implements {@link HeaderResourceProducer}) and recursive process all
	 * facets and childrens.
	 * 
	 * @param context
	 *            TODO
	 * @param root
	 */
	private void processHeadResources(FacesContext context, UIComponent root) {
		Renderer renderer = getRenderer(context, root);
		if (null != renderer) {
			if ((processScripts || processStyles ) && renderer instanceof HeaderResourceProducer) {
				HeaderResourceProducer producer = (HeaderResourceProducer) renderer;
				if (processScripts) {
					Set<String> set = producer.getHeaderScripts(context, root);
					if (null != set) {
						scripts.addAll(set);
					}

				}
				if (processStyles) {
					Set<String> set = producer.getHeaderStyles(context, root);
					if (null != set) {
						styles.addAll(set);
					}

				}
			} else if (renderer instanceof UserResourceRenderer) {
				UserResourceRenderer producer = (UserResourceRenderer) renderer;
				Set<String> set = producer.getHeaderScripts(context, root);
				if (null != set) {
					userScripts.addAll(set);
				}
				set = producer.getHeaderStyles(context, root);
				if (null != set) {
					userStyles.addAll(set);
				}
			}

		}
		for (Iterator<UIComponent> iter = root.getFacets().values().iterator(); iter
				.hasNext();) {
			UIComponent child = iter.next();
			processHeadResources(context, child);
		}
		for (Iterator<UIComponent> iter = root.getChildren().iterator(); iter.hasNext();) {
			UIComponent child = iter.next();
			processHeadResources(context, child);
		}
	}

	/**
	 * Find renderer for given component.
	 * 
	 * @param context
	 * @param comp
	 * @param renderKit
	 * @return
	 */
	private Renderer getRenderer(FacesContext context, UIComponent comp) {

		String rendererType = comp.getRendererType();
		if (rendererType != null) {
			return (renderKit.getRenderer(comp.getFamily(), rendererType));
		} else {
			return (null);
		}

	}

	/**
	 * @return the processStyles
	 */
	public boolean isProcessStyles() {
		return processStyles;
	}

	/**
	 * @param processStyles the processStyles to set
	 */
	public void setProcessStyles(boolean processStyles) {
		this.processStyles = processStyles;
	}

	/**
	 * @return the processScripts
	 */
	public boolean isProcessScripts() {
		return processScripts;
	}

	/**
	 * @param processScripts the processScripts to set
	 */
	public void setProcessScripts(boolean processScripts) {
		this.processScripts = processScripts;
	}

	/**
	 * @return the scripts
	 */
	public Set<String> getScripts() {
		return scripts;
	}

	/**
	 * @return the styles
	 */
	public Set<String> getStyles() {
		return styles;
	}

	/**
	 * @return user styles
	 */
	public Set<String> getUserStyles() {
		return userStyles;
	}
	
	public void addScript(String scriptUrl) {
		scripts.add(scriptUrl);
	}

	public void addStyle(String styleUrl) {
		styles.add(styleUrl);
	}
}
