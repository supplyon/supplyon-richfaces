/**
 * 
 */
package org.richfaces.model.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.richfaces.model.SortOrder;

/**
 * @author Maksim Kaszynski
 *
 */
public class ArrayDataModel extends SimpleGridDataModel {

	private Object [] data;

	public ArrayDataModel() {
		this(null);
	}
	
	public ArrayDataModel(Object [] data) {
		super();
		setWrappedData(data);
	}

	

	/* (non-Javadoc)
	 * @see org.richfaces.model.ScrollableTableDataModel#loadData(int, int, org.richfaces.model.SortOrder)
	 */
	public List loadData(int startRow, int endRow, SortOrder sortOrder) {
		
		if (data != null && getRowCount() > 0) {
			Object [] sortedList = data;
			
			if (sortOrder != null) {

				sortedList = new Object[data.length];
				
				System.arraycopy(data, 0, sortedList, 0, data.length);
				
				Comparator comparator = createComparator(sortOrder);
				
				if (comparator == null) {
					
					Arrays.sort(sortedList);
				
				} else {
					
					Arrays.sort(sortedList, comparator);
				
				}
				
			}
			
			Object [] subArray = new Object[endRow - startRow];
			
			System.arraycopy(sortedList, startRow, subArray, 0, subArray.length);
			
			return Arrays.asList(subArray);
		}
		
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	public int getRowCount() {
		return  data == null ? 0 : data.length;
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
		this.data = (Object[]) data;
	}
}
