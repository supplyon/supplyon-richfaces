/**
 * 
 */
package org.richfaces.component;

import java.util.List;

import org.richfaces.model.SortField2;

/**
 * @author Maksim Kaszynski
 *
 */
public interface Sortable2 {
	
	public List<SortField2> getSortFields();
	
	public void setSortFields(List<SortField2> sortFields);
	
}
