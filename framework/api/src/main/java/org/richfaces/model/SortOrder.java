package org.richfaces.model;

import java.io.Serializable;
import java.util.Arrays;
/**
 * Class representing grid sort order
 * Sort Order is the combination of {@link SortField}
 * Order of occurrence of sort fields must be maintained
 * to guarantee stable sorting 
 * @author Maksim Kaszynski
 */
public class SortOrder implements Serializable {

	private static final long serialVersionUID = 2423450561570551363L;

	private static int hashCode(Object[] array) {
		final int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result
					+ (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	private SortField [] fields;

	public SortOrder() {
		
	}
	
	public SortOrder(SortField[] fields) {
		super();
		this.fields = fields;
	}



	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SortOrder other = (SortOrder) obj;
		if (!Arrays.equals(fields, other.fields))
			return false;
		return true;
	}

	public SortField[] getFields() {
		return fields;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + SortOrder.hashCode(fields);
		return result;
	}

	public void setFields(SortField[] fields) {
		this.fields = fields;
	}

	
	
}