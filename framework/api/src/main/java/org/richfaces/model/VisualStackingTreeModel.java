/**
 * 
 */
package org.richfaces.model;

import javax.faces.component.UIComponent;

/**
 * That is intended for internal use
 * 
 * @author Nick Belaevski
 *         mailto:nbelaevski@exadel.com
 *         created 15.08.2007
 *
 */
public class VisualStackingTreeModel extends StackingTreeModel implements TreeModelVisualComponentProvider {
	private UIComponent component;

	public VisualStackingTreeModel(UIComponent component) {
		super();

		this.component = component;
	}

	public VisualStackingTreeModel(String id, String var,
			StackingTreeModelDataProvider dataProvider, UIComponent component) {
		super(id, var, dataProvider);

		this.component = component;
	}
	
	public UIComponent getComponent() {
		if (this.component != null) {
			return this.component;
		}

		StackingTreeModel currentModel = getCurrentModel();
		if (currentModel != null && currentModel != this) {
			return ((TreeModelVisualComponentProvider) currentModel).getComponent();
		}

		return null;
	}
}
