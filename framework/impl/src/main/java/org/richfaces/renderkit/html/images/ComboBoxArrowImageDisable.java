/**
 * 
 */
package org.richfaces.renderkit.html.images;

import javax.faces.context.FacesContext;

/**
 * @author Anton Belevich
 * @since 3.2.0
 * ComboBox disable arrow image renderer 
 *
 */
public class ComboBoxArrowImageDisable extends ComboBoxArrowImage{

	protected Object storeData(FacesContext context, String colorSkinParam, String backgroundSkinParam, String borderSkinParam) {
		return super.storeData(context, DISABLED_ICON_COLOR, BACKGROUND_COLOR, DISABLED_BORDER_COLOR);
	}
}
