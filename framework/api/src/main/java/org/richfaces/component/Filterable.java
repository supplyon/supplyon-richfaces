/**
 * 
 */
package org.richfaces.component;

import java.util.List;

import org.richfaces.model.FilterField;

/**
 * @author Konstantin Mishin
 *
 */
public interface Filterable {
	
	public List<FilterField> getFilterFields();
	
	public void setFilterFields(List<FilterField> filterFields);
	
}
