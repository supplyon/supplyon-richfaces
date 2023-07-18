/**
 * 
 */
package org.richfaces.model;

import javax.el.Expression;

/**
 * @author Konstantin Mishin
 *
 */
public class ExtendedFilterField extends FilterField{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5443560922389498666L;
	
	private String filterValue;

	public ExtendedFilterField(Expression expression, String filterValue) {
		super(expression);
		this.filterValue = filterValue;
	}

	public ExtendedFilterField(Expression expression) {
		this(expression, "");
	}
	
	public String getFilterValue() {
		return filterValue;
	}
}
