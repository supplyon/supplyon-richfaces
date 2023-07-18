/**
 * 
 */
package org.richfaces.webapp.taglib;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;

/**
 * Maps {@link MethodExpression} to {@link MethodBinding}
 * @author Maksim Kaszynski
 *
 */
@SuppressWarnings("deprecation")
public class MethodBindingMethodExpressionAdaptor extends MethodBinding implements StateHolder{
	
	private MethodExpression expression;
	private boolean tranzient;
	/* (non-Javadoc)
	 * @see javax.faces.el.MethodBinding#getType(javax.faces.context.FacesContext)
	 */
	public MethodBindingMethodExpressionAdaptor() {
		// TODO Auto-generated constructor stub
	}
	
	public MethodBindingMethodExpressionAdaptor(MethodExpression expression) {
		super();
		this.expression = expression;
	}

	@Override
	public Class<?> getType(FacesContext context) throws MethodNotFoundException {
		try {
			return expression.getMethodInfo(context.getELContext()).getReturnType();
		} catch (javax.el.MethodNotFoundException e) {
			throw new MethodNotFoundException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.el.MethodBinding#invoke(javax.faces.context.FacesContext, java.lang.Object[])
	 */
	@Override
	public Object invoke(FacesContext context, Object[] params)
			throws EvaluationException, MethodNotFoundException {
		try {
			return expression.invoke(context.getELContext(), params);
		} catch (javax.el.MethodNotFoundException e) {
			throw new MethodNotFoundException(e);
		} catch (ELException e) {
			throw new EvaluationException(e);
		}
	}

	public boolean isTransient() {
		return tranzient;
	}
	
	public void restoreState(FacesContext context, Object state) {
		expression = (MethodExpression) state;
	}
	
	public Object saveState(FacesContext context) {
		return expression;
	}
	
	public void setTransient(boolean newTransientValue) {
		tranzient = newTransientValue;
	}

	@Override
	public String getExpressionString() {
		return expression.getExpressionString();
	}
	
	
}
