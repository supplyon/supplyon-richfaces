/**
 * 
 */
package org.richfaces.event.sort;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

/**
 * @author Maksim Kaszynski
 *
 */
public class SortEvent2 extends FacesEvent{

	private static final long serialVersionUID = -3141067055845421505L;

	//TODO: making ValueExpression here - should redesign SortOrder/SortField?
	private ValueExpression sortExpression;
	
	private boolean force;

	public SortEvent2(UIComponent component, ValueExpression sortExpression,
			boolean force) {
		super(component);
		this.sortExpression = sortExpression;
		this.force = force;
	}
	
	public SortEvent2(UIComponent component, ValueExpression sortExpression) {
		this(component, sortExpression, false);
	}
	
	@Override
	public boolean isAppropriateListener(FacesListener listener) {
		return listener instanceof SortListener2;
	}
	
	@Override
	public void processListener(FacesListener listener) {
		((SortListener2) listener).processSorting(this);
	}
	
	public boolean isForce() {
		return force;
	}
	
	public ValueExpression getSortExpression() {
		return sortExpression;
	}
}
