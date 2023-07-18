/**
 * 
 */
package org.richfaces.component;

import org.richfaces.model.selection.Selection;

/**
 * @author Maksim Kaszynski
 *
 */
public interface Selectable {
	public Selection getSelection();
	public void setSelection (Selection selection);
}
