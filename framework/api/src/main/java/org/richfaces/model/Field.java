/**
 * 
 */
package org.richfaces.model;

import java.io.Serializable;

import javax.el.Expression;

/**
 * @author Konstantin Mishin
 *
 */
public abstract class Field implements Serializable{

	private static final long serialVersionUID = 7576046308828980778L;
	
	private Expression expression;
	
	public Field(Expression expression) {
		this.expression = expression;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else {
			return expression.equals(other.expression);
		}
		return true;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}	
}
