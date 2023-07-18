/**
 * 
 */
package org.richfaces.model;

import java.io.Serializable;

/**
 * Special type of row key containing information on item origin and new placement
 * 
 * @author Nick Belaevski
 *
 */
public class ListShuttleRowKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3308741255288495879L;
	
	private boolean source;
	
	private boolean facadeSource;
	
	private Object rowKey;

	public boolean isSource() {
		return source;
	}
	
	public boolean isFacadeSource() {
		return facadeSource;
	}
	
	public Object getRowKey() {
		return rowKey;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rowKey == null) ? 0 : rowKey.hashCode());
		result = prime * result + (source ? 1231 : 1237);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListShuttleRowKey other = (ListShuttleRowKey) obj;
		if (rowKey == null) {
			if (other.rowKey != null)
				return false;
		} else if (!rowKey.equals(other.rowKey))
			return false;
		if (source != other.source)
			return false;
		return true;
	}
	
	public String toString() {
		return (source ? "" : "t") + rowKey.toString();
	}

	public ListShuttleRowKey(Object rowKey, boolean source) {
		super();
		this.rowKey = rowKey;
		this.source = source;
		this.facadeSource = source;
	}

	public ListShuttleRowKey(Object rowKey, boolean source, boolean facadeSource) {
		super();
		this.rowKey = rowKey;
		this.source = source;
		this.facadeSource = facadeSource;
	}
}
