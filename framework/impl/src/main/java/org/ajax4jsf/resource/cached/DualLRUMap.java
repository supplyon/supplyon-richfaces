/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.ajax4jsf.resource.cached;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.LRUMap;

class DualLRUMap extends LRUMap {
	
	private Map reverseMap ;

	public DualLRUMap(int size) {
		super(size);
		reverseMap = new HashMap(size);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.LRUMap#processRemovedLRU(java.lang.Object, java.lang.Object)
	 */
	protected void processRemovedLRU(Object key, Object value) {
		synchronized (this) {
		super.processRemovedLRU(key, value);
		reverseMap.remove(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.LRUMap#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value) {
		synchronized (this) {
			reverseMap.put(value, key);
			return super.put(key, value);				
		}
	}
	
	public Object getKey(Object value){
		synchronized (this) {
		Object key = reverseMap.get(value);
		if(!containsKey(key)){
			reverseMap.remove(value);
			key = null;
		}
		return key;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.LRUMap#get(java.lang.Object)
	 */
	public Object get(Object key) {
		synchronized (this) {
		return super.get(key);
		}
	}
	
}