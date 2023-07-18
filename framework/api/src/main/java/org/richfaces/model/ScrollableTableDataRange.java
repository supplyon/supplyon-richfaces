/*
 *  Copyright
 *      Copyright (c) Exadel,Inc. 2006
 *      All rights reserved.
 *  
 *  History
 *      $Source: /cvs-master/intralinks-jsf-comps/components/data-view-grid/src/component/com/exadel/jsf/model/ScrollableTableDataRange.java,v $
 *      $Revision: 1.7 $ 
 */

package org.richfaces.model;

import java.io.Serializable;

import org.ajax4jsf.model.Range;

/**
 * Iteration range for Scrollable Grid
 * @author Maksim Kaszynski
 * @modified by Anton Belevich
 */
public class ScrollableTableDataRange implements Range, Serializable{
	
	private static final long serialVersionUID = -6675002421400464892L;
	
	private int first = 0;
	private int last = 0;
	private SortOrder sortOrder;
	
	public ScrollableTableDataRange(int first, int last, SortOrder sortOrder) {
		super();
		this.first = first;
		this.last = last;
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the bufferSize
	 */
	public int getLast() {
		return last;
	}
	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setLast(int lastRow) {
		this.last = lastRow;
	}
	/**
	 * @return the first
	 */
	public int getFirst() {
		return first;
	}
	/**
	 * @param first the first to set
	 */
	public void setFirst(int first) {
		this.first = first;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public boolean equals(Object obj) {
	
		if(!(obj instanceof ScrollableTableDataRange)){
			return super.equals(obj);
		}
		
		ScrollableTableDataRange ref = (ScrollableTableDataRange)obj;
		
		boolean ret = (this.first == ref.first)&&(this.last == ref.last );
		
		if(this.sortOrder != null){
			ret = ret && this.sortOrder.equals(ref.sortOrder);  
		} else {
			ret = (ret && (ref.sortOrder == null));
		}
				
		return ret;
	}
	
	public int hashCode() {
		return super.hashCode();
	}
}
