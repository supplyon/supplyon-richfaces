/**
 * 
 */
package org.richfaces.event.scroll;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesListener;

import org.richfaces.event.ScrollableGridViewEvent;


/**
 * @author Anton Belevich
 *
 */
public class ScrollEvent extends ScrollableGridViewEvent {

	private static final long serialVersionUID = 3786221668771853810L;

	public ScrollEvent(UIComponent component, int rows, int first){
		super(component, rows, first);
		this.rows = rows;
		this.first = first;
	}

	public boolean isAppropriateListener(FacesListener listener) {
		return listener instanceof ScrollListener;
	}

	public void processListener(FacesListener listener) {
		((ScrollListener) listener).processScroll(this);
	}

}
