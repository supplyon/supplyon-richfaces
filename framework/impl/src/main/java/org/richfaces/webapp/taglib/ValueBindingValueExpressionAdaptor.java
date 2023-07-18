/**
 * 
 */
package org.richfaces.webapp.taglib;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

/**
 * @author Maksim Kaszynski
 *
 */
@SuppressWarnings("deprecation")
public class ValueBindingValueExpressionAdaptor extends ValueBinding implements StateHolder {

	private ValueExpression expression;
	private boolean tranzient;
	
	/* (non-Javadoc)
	 * @see javax.faces.el.ValueBinding#getType(javax.faces.context.FacesContext)
	 */
	@Override
	public Class<?> getType(FacesContext context) throws EvaluationException,
			PropertyNotFoundException {
		try {
			return expression.getType(context.getELContext());
		} catch (javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(e);
		} catch (ELException e) {
			throw new EvaluationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.el.ValueBinding#getValue(javax.faces.context.FacesContext)
	 */
	@Override
	public Object getValue(FacesContext context) throws EvaluationException,
			PropertyNotFoundException {
		try {
			return expression.getValue(context.getELContext());
		} catch(javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(e);
		} catch (ELException e) {
			throw new EvaluationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.el.ValueBinding#isReadOnly(javax.faces.context.FacesContext)
	 */
	@Override
	public boolean isReadOnly(FacesContext context) throws EvaluationException,
			PropertyNotFoundException {
		try {
			return expression.isReadOnly(context.getELContext());
		} catch(javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(e);
		} catch (ELException e) {
			throw new EvaluationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.el.ValueBinding#setValue(javax.faces.context.FacesContext, java.lang.Object)
	 */
	@Override
	public void setValue(FacesContext context, Object value)
			throws EvaluationException, PropertyNotFoundException {
		
		try {
			expression.setValue(context.getELContext(), value);
		} catch(javax.el.PropertyNotFoundException e) {
			throw new PropertyNotFoundException(e);
		} catch (ELException e) {
			throw new EvaluationException(e);
		}
		
	}
	
	public boolean isTransient() {
		return tranzient;
	}
	
	public void restoreState(FacesContext context, Object state) {
		expression = (ValueExpression) state;
	}
	
	public Object saveState(FacesContext context) {
		return expression;
	}
	
	public void setTransient(boolean newTransientValue) {
		tranzient = newTransientValue;
	}

	public ValueExpression getExpression() {
		return expression;
	}

	public void setExpression(ValueExpression expression) {
		this.expression = expression;
	}
	
	public ValueBindingValueExpressionAdaptor() {
		// TODO Auto-generated constructor stub
	}

	public ValueBindingValueExpressionAdaptor(ValueExpression expression) {
		super();
		this.expression = expression;
	}


}
