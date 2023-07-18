/**
 * 
 */
package org.ajax4jsf.webapp.taglib;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

/**
 * This if "empty" facelets tag handler stub. This handler used to ignore tags on a page.
 * At most, it used to bypass &lt;jsp:root&gt; and other tags, so we can use same markup for an facelets an jsp pages.
 * @author asmirnov
 *
 */
public class EmptyHandler extends TagHandler {

	public EmptyHandler(TagConfig config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, javax.faces.component.UIComponent)
	 */
	public void apply(FaceletContext ctx, UIComponent parent)
			throws IOException, FacesException, FaceletException, ELException {
		this.nextHandler.apply(ctx, parent);

	}

}
