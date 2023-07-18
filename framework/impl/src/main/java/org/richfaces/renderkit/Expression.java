/**
 * 
 */
package org.richfaces.renderkit;

/**
 * @author Nick Belaevski - mailto:nbelaevski@exadel.com
 * created 20.06.2007
 *
 */
public class Expression {
	private Object expression;

	public Expression(Object expression) {
		super();
		this.expression = expression;
	}

	public Object getExpression() {
		return expression;
	}
	
	public String toString() {
		return super.toString() + "[" + expression + "]";
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Expression other = (Expression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}
}
