/**
 * 
 */
package org.richfaces.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;

/**
 * Map-based extended data model for model-translating components like ordering list
 * 
 * @author Nick Belaevski
 *         mailto:nbelaevski@exadel.com
 *         created 07.11.2007
 *
 */
public class OrderingListDataModel extends ExtendedDataModel {

	private Map<Object, Object> data;
	
	private Object rowKey;
	
	public Object getRowKey() {
		return rowKey;
	}

	public void setRowKey(Object rowKey) {
		this.rowKey = rowKey;
	}

	public void walk(FacesContext context, DataVisitor visitor, Range range,
			Object argument) throws IOException {

		Set<Entry<Object,Object>> entrySet = data.entrySet();
		Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
		
		while (iterator.hasNext()) {
			Entry<Object, Object> entry = iterator.next();
			
			visitor.process(context, entry.getKey(), argument);
		}
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getRowData() {
		return data.get(rowKey);
	}

	public int getRowIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getWrappedData() {
		return data;
	}

	public boolean isRowAvailable() {
		return data.containsKey(rowKey);
	}

	public void setRowIndex(int rowIndex) {
		// TODO Auto-generated method stub
		
	}

	public void setWrappedData(Object data) {
		this.rowKey = null;
		this.data = (Map<Object, Object>) data;
	}

}
