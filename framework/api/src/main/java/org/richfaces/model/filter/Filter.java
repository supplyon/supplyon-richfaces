/**
 * 
 */
package org.richfaces.model.filter;

/**
 * @author Maksim Kaszynski
 *
 */
public interface Filter<T> {
	public boolean accept(T t);
}
