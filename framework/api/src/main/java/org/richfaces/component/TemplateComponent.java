/**
 * 
 */
package org.richfaces.component;

/**
 * @author Nick Belaevski
 *         mailto:nbelaevski@exadel.com
 *         created 10.08.2007
 *
 */
public interface TemplateComponent {
	public static final String TEMPLATE_CLIENT_ID = "{componentId}";
	
	public void startTemplateEncode();
	
	public void endTemplateEncode();
}
