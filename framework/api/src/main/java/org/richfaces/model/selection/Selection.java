/**
 * 
 */
package org.richfaces.model.selection;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author Maksim Kaszynski
 *
 */
public interface Selection extends Serializable {
	
	public Iterator<Object> getKeys();
	
	public int size();
	
	public boolean isSelected(Object rowKey);
}
