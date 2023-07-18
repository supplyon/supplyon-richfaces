package org.richfaces.event;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

public class CurrentDateChangeEvent extends FacesEvent {

	
	private static final long serialVersionUID = -8169207286087810907L;
	private Date currentDate = null;
	private String currentDateString = null;

	public CurrentDateChangeEvent(UIComponent component, Date curentDate) {
		super(component);
		this.currentDate = curentDate;

	}
	public CurrentDateChangeEvent(UIComponent component, String curentDateString) {
		super(component);
		this.currentDateString = curentDateString;		 
	}

	public boolean isAppropriateListener(FacesListener listener) {
		return false;
	}

	public void processListener(FacesListener listener) {
		// TODO Auto-generated method stub		
		throw new UnsupportedOperationException();
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public String getCurrentDateString() {
		return currentDateString;
	}

}
