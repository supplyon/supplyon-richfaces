/**
 * 
 */
package org.richfaces.webapp.taglib;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.el.MethodBinding;

/**
 * @author Maksim Kaszynski
 *
 */
public class SimpleComponentTag extends UIComponentELTagBase {

	private MethodExpression _action;
	private MethodExpression _actionListener;
	private MethodExpression _valueChangeListener;
	private MethodExpression _validator;
	private ValueExpression _converter;
	private ValueExpression _title;
	private ValueExpression _value;
	private MethodExpression _legacyBinding;
	private MethodExpression _methodExpression;
	
	static class MyUIComponent extends UIComponentBase {
		private MethodExpression methodExpression;
		private MethodBinding legacyMethodBinding;

		@Override
		public String getFamily() {
			// TODO Auto-generated method stub
			return null;
		}
		public MethodBinding getLegacyMethodBinding() {
			return legacyMethodBinding;
		}

		public void setLegacyMethodBinding(MethodBinding legacyMethodBinding) {
			this.legacyMethodBinding = legacyMethodBinding;
		}

		public MethodExpression getMethodExpression() {
			return methodExpression;
		}

		public void setMethodExpression(MethodExpression methodExpression) {
			this.methodExpression = methodExpression;
		}
		
	}
	
	public SimpleComponentTag() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getComponentType() {
		return UIInput.COMPONENT_TYPE;
	}

	@Override
	public String getRendererType() {
		return "javax.faces.Text";
	}

	@Override
	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		MyUIComponent myUIComponent = (MyUIComponent) component;
		
		myUIComponent.setValueExpression("value", _value);
		
		setActionListenerProperty(myUIComponent, _actionListener);
		setActionProperty(myUIComponent, _action);
		setConverterProperty(myUIComponent, _converter);
		setValidatorProperty(myUIComponent, _validator);
		
		
		if (_methodExpression != null) {
			myUIComponent.setMethodExpression(_methodExpression);
		}
		
		if (_legacyBinding != null) {
			myUIComponent.setLegacyMethodBinding(new MethodBindingMethodExpressionAdaptor(_legacyBinding));
		}
		
		
	}
	
	@Override
	public void release() {
		super.release();
		
	}
	
	public void setAction(MethodExpression _action) {
		this._action = _action;
	}

	public MethodExpression getAction() {
		return _action;
	}


	public MethodExpression getActionListener() {
		return _actionListener;
	}

	public void setActionListener(MethodExpression listener) {
		_actionListener = listener;
	}

	public MethodExpression getValueChangeListener() {
		return _valueChangeListener;
	}

	public void setValueChangeListener(MethodExpression changeListener) {
		_valueChangeListener = changeListener;
	}

	public MethodExpression getValidator() {
		return _validator;
	}

	public void setValidator(MethodExpression _validator) {
		this._validator = _validator;
	}

	public ValueExpression getConverter() {
		return _converter;
	}

	public void setConverter(ValueExpression _converter) {
		this._converter = _converter;
	}

	public ValueExpression getTitle() {
		return _title;
	}

	public void setTitle(ValueExpression _title) {
		this._title = _title;
	}

	public ValueExpression getValue() {
		return _value;
	}

	public void setValue(ValueExpression _value) {
		this._value = _value;
	}
	
	
}
