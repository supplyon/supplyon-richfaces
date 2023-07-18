/**
 * 
 */
package org.richfaces.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.richfaces.model.SortOrder;

/**
 * @author Maksim Kaszynski
 *
 */
public class ListDataModel extends SimpleGridDataModel {

	private List data;

	public ListDataModel() {
		this(null);
	}
	
	public ListDataModel(List data) {
		super();
		setWrappedData(data);
	}

	

	/* (non-Javadoc)
	 * @see org.richfaces.model.ScrollableTableDataModel#loadData(int, int, org.richfaces.model.SortOrder)
	 */
	public List loadData(int startRow, int endRow, SortOrder sortOrder) {
		
		if (data != null && getRowCount() > 0) {
			List sortedList = data;
			
			if (sortOrder != null) {
				sortedList = new ArrayList(data);
				
				Comparator comparator = createComparator(sortOrder);
				
				if (comparator == null) {
					
					Collections.sort(sortedList);
				
				} else {
					
					Collections.sort(sortedList, comparator);
				
				}
				
			}
			
			return sortedList.subList(startRow, endRow);
		}
		
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	public int getRowCount() {
		return  data == null ? 0 : data.size();
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	public Object getWrappedData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	public void setWrappedData(Object data) {
		this.data = (List) data;
	}

}
