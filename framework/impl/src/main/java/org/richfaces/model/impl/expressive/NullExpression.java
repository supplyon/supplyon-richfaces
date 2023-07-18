package org.richfaces.model.impl.expressive;

/**
 * @author Maksim Kaszynski
 *
 */
final class NullExpression extends Expression {
	/**
	 * @param n
	 */
	NullExpression(String name) {
		super(name);
	}

	public Object evaluate(Object base) {
		return null;
	}
}