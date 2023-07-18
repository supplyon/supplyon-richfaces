/**
 * 
 */
package org.richfaces.component.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Nick Belaevski
 *         mailto:nbelaevski@exadel.com
 *         created 20.07.2007
 *
 */
public class ComponentUtil {
	public static String[] asArray(Object object) {
		if (object == null) {
			return null;
		}
		
		Class componentType = object.getClass().getComponentType();
		
		if (String.class.equals(componentType)) {
			return (String[]) object;
		} else if (componentType != null) {
			Object[] objects = (Object[]) object;
			String[] result = new String[objects.length];
			for (int i = 0; i < objects.length; i++) {
				Object o = objects[i];
				if (o == null) {
					continue;
				}
				
				result[i] = o.toString();
			}
			
			return result;
		} else if (object instanceof Collection) {
			Collection collection = (Collection) object;
			String[] result = new String[collection.size()]; 
			Iterator iterator = collection.iterator();
			
			for (int i = 0; i < result.length; i++) {
				Object next = iterator.next();
				if (next == null) {
					continue;
				}
				
				result[i] = next.toString();
			}
			
			return result;
		} else {
			String string = object.toString().trim();
			String[] split = string.split("\\s*,\\s*");
			return split;
		}
	}
}
