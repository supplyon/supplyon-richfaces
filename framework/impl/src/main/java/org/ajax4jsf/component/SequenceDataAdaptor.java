/**
 * 
 */
package org.ajax4jsf.component;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ResultDataModel;
import javax.faces.model.ResultSetDataModel;
import javax.faces.model.ScalarDataModel;
import javax.servlet.jsp.jstl.sql.Result;

import org.ajax4jsf.model.DataComponentState;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceDataModel;
import org.ajax4jsf.model.SequenceRange;

/**
 * @author asmirnov
 *
 */
public abstract class SequenceDataAdaptor extends UIDataAdaptor {

	protected DataComponentState createComponentState() {
		// Create component state based on this instance.
		return new DataComponentState(){
	
			public Range getRange() {
				return new SequenceRange(getFirst(),getRows());
			}
		};
		
	}

	protected ExtendedDataModel createDataModel() {
		return (ExtendedDataModel) getDataModel();
	}

	protected DataModel getDataModel() {
	    // Synthesize a DataModel around our current value if possible
		// TODO - for jsf 1.2 use method from superclass ?
	    Object current = getValue();
	    DataModel model;
	    if (current == null) {
	        model = new SequenceDataModel(new ListDataModel(Collections.EMPTY_LIST));
	    } else if (current instanceof ExtendedDataModel) {
	        model = (DataModel) current;
	    } else if (current instanceof DataModel) {
	        model = new SequenceDataModel((DataModel) current);
	    } else if (current instanceof List) {
	        model = new SequenceDataModel(new ListDataModel((List<?>) current));
	    } else if (Object[].class.isAssignableFrom(current.getClass())) {
	        model = new SequenceDataModel(new ArrayDataModel((Object[]) current));
	    } else if (current instanceof ResultSet) {
	        model = new SequenceDataModel(new ResultSetDataModel((ResultSet) current));
	    } else if (current instanceof Result) {
	        model = new SequenceDataModel(new ResultDataModel((Result) current));
	    } else {
	        model = new SequenceDataModel(new ScalarDataModel(current));
	    }
	return (model);
	}

	protected void setDataModel(DataModel model) {
		ExtendedDataModel iterableModel;
		if( null == model){
			iterableModel = null;
		} else if (model instanceof ExtendedDataModel) {
			iterableModel = (ExtendedDataModel) model;			
		} else {
			iterableModel = new SequenceDataModel(model);
		}
		setExtendedDataModel(iterableModel);
	}


}
