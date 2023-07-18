package org.richfaces.model.impl.expressive;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.context.FacesContext;

/**
 * 
 * @author Konstantin Mishin
 *
 */
public class MethodBindingExpression extends Expression {

	private ELContext context;
	private MethodExpression methodExpression;

	public MethodBindingExpression(FacesContext faces, MethodExpression methodExpression) {
		super(methodExpression.getExpressionString());
		this.context = faces.getELContext();
		this.methodExpression = methodExpression;
	}

	public Object evaluate(Object base) {
		return methodExpression.invoke(context, new Object[]{base});
	}
}