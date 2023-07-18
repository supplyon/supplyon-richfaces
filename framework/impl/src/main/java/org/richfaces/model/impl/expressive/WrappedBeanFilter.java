/**
 * 
 */
package org.richfaces.model.impl.expressive;

import java.util.List;

import org.richfaces.model.ExtendedFilterField;
import org.richfaces.model.FilterField;

/**
 * @author Maksim Kaszynski
 *
 */
public class WrappedBeanFilter implements org.richfaces.model.filter.Filter<JavaBeanWrapper>{
	
	private final List<FilterField> filterFields;

	public WrappedBeanFilter(List<FilterField> filterFields) {
		this.filterFields = filterFields;
	}

	public boolean accept(JavaBeanWrapper wrapper) {
		for (FilterField filterField : filterFields) {
			if (filterField instanceof ExtendedFilterField) {
				Object property = wrapper.getProperty(filterField.getExpression().getExpressionString());
				String filterValue = ((ExtendedFilterField)filterField).getFilterValue();
				if(filterValue != null && filterValue.length() > 0) {
					filterValue = filterValue.trim().toUpperCase();
					if(property == null || !property.toString().trim().toUpperCase().startsWith(filterValue)) {
						return false;
					}	
				}				
			} else {
				Object property = wrapper.getProperty(filterField.getExpression().getExpressionString());
				if(!((Boolean)property).booleanValue()) {
					return false;
				}
			}
		}
		return true;
	}

}
