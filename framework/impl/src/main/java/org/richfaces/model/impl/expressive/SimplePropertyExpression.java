package org.richfaces.model.impl.expressive;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;

/**
 * Expression evaluated by applying application
 * property resolver to the base object
 * @author Maksim Kaszynski
 *
 */
final class SimplePropertyExpression extends Expression {
	/**
	 * 
	 */
	private final ELResolver resolver;
	private final ELContext context;
	
	/**
	 * @param n
	 * @param resolver
	 */
	SimplePropertyExpression(String n, ELContext context, ELResolver resolver) {
		super(n);
		this.resolver = resolver;
		this.context = context;
	}

	public Object evaluate(Object base) {
		Object o = null;
		try {
			return resolver.getValue(context, base, getExpressionString());
		} catch (ELException e) {
			
		} 
		
		return o;
	}
}