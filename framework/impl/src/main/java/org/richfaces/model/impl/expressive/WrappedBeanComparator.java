package org.richfaces.model.impl.expressive;

import java.util.Comparator;

import org.richfaces.model.SortField;

/**
 * Comparator for {@link JavaBeanWrapper} objects.
 * Compares them using {@link SortField} sequence.
 * 
 * @author Maksim Kaszynski
 *
 */
public final class WrappedBeanComparator implements Comparator<Object> {
	/**
	 * 
	 */
	private final SortField[] fields;

	/**
	 * @param fields
	 */
	public WrappedBeanComparator(SortField[] fields) {
		this.fields = fields;
	}
	
	public int compare(Object o1, Object o2) {
		return compare((JavaBeanWrapper) o1, (JavaBeanWrapper) o2);
	}
	
	@SuppressWarnings("unchecked")
	private int compare(JavaBeanWrapper w1, JavaBeanWrapper w2) {
		
		int result = 0;
		
		for (int i = 0; i < fields.length && result == 0; i++) {
			
			String prop = fields[i].getName();
			Boolean asc = fields[i].getAscending();
			
			Object p1 = w1.getProperty(prop);
			Object p2 = w2.getProperty(prop);
			
			if (p1 instanceof Comparable && p2 instanceof Comparable) {
				result = ((Comparable<Object>) p1).compareTo(p2);
			} else if (p1 == null && p2 != null) {
				result = 1;
			} else if (p2 == null && p1 != null) {
				result = -1;
			}
			
			if (asc != null && !asc.booleanValue()) {
				result = -result;
			}
			
		}
		
		
		return result;
	}
}