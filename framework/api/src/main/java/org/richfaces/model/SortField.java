/**
 * 
 */
package org.richfaces.model;

import java.io.Serializable;

/**
 * Sort field is the piece of {@link SortOrder}
 * @author Maksim Kaszynski
 *
 */
public class SortField implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name = null;
	private Boolean ascending = null;
	
	
	
	public SortField(String name, Boolean ascending) {
		super();
		this.name = name;
		this.ascending = ascending;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ascending == null) ? 0 : ascending.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortField other = (SortField) obj;
		if (ascending == null) {
			if (other.ascending != null)
				return false;
		} else if (!ascending.equals(other.ascending))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
