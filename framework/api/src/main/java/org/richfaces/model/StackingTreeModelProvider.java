/**
 * 
 */
package org.richfaces.model;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;


/**
 * That is intended for internal use
 * 
 * @author Nick Belaevski mailto:nbelaevski@exadel.com created 25.07.2007
 * 
 */
public abstract class StackingTreeModelProvider extends UIComponentBase {

	public abstract Object getNodes();
	public abstract void setNodes(Object nodes);

	public Object getData() {
		return getNodes();
	}
	
	protected abstract StackingTreeModel createStackingTreeModel();

	public StackingTreeModel getStackingModel() {
		StackingTreeModel stackingTreeModel = createStackingTreeModel();
		if (getChildCount() > 0) {
			Iterator children = getChildren().iterator();
			while (children.hasNext()) {
				UIComponent component = (UIComponent) children.next();
				if (component instanceof StackingTreeModelProvider) {
					StackingTreeModelProvider provider = (StackingTreeModelProvider) component;
					
					stackingTreeModel.addStackingModel(provider.getStackingModel());
				}
			}
		}
		
		return stackingTreeModel;
	}
}
