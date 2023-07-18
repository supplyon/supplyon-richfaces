/**
 * 
 */
package org.ajax4jsf.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Last Recent Used Map cache. See {@link LinkedHashMap} for details.
 * @author asmirnov
 *
 */
public class LRUMap<K,V> extends LinkedHashMap<K,V> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7232885382582796665L;
	private int capacity;
	
	
	/**
	 * @param capacity - maximal cache capacity.
	 */
	public LRUMap(int capacity) {
		super(capacity, 1.0f,true);
		this.capacity = capacity;
	}

	
	protected boolean removeEldestEntry(Entry<K,V> entry) {
		// Remove last entry if size exceeded.
		return size()>capacity;
	}

	/**
	 * Get most recent used element 
	 * @return the most Recent value
	 */
	public V getMostRecent() {
		Iterator<V> iterator = values().iterator();
		V mostRecent=null;
		while (iterator.hasNext()) {
			 mostRecent = iterator.next();
			
		}
		return mostRecent;
	}
}
