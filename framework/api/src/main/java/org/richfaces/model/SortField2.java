/**
 * 
 */
package org.richfaces.model;

import javax.el.Expression;

/**
 * @author Maksim Kaszynski
 *
 */
public class SortField2 extends Field{

	private static final long serialVersionUID = 4578290842517554179L;
	
	private Ordering ordering;
	
	public SortField2(Expression expression) {
		super(expression);
	}
	
	public SortField2(Expression expression, Ordering ordering) {
		super(expression);
		this.ordering = ordering;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((ordering == null) ? 0 : ordering.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortField2 other = (SortField2) obj;
		if (ordering == null) {
			if (other.ordering != null)
				return false;
		} else if (!ordering.equals(other.ordering))
			return false;
		return true;
	}

	public Ordering getOrdering() {
		return ordering;
	}

	public void setOrdering(Ordering ordering) {
		this.ordering = ordering;
	}
	
}
