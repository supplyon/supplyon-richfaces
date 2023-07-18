package org.richfaces.model.impl.expressive;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.el.Expression;

import org.richfaces.model.Ordering;
import org.richfaces.model.SortField2;

/**
 * Comparator for {@link JavaBeanWrapper} objects.
 * Compares them using {@link SortField} sequence.
 * 
 * @author Maksim Kaszynski
 *
 */
public final class WrappedBeanComparator2 implements Comparator<Object> {
	
	private final List<SortField2> sortFields;

	public WrappedBeanComparator2(List<SortField2> sortFields) {
		super();
		this.sortFields = sortFields;
	}

	public int compare(Object o1, Object o2) {
		return compare((JavaBeanWrapper) o1, (JavaBeanWrapper) o2);
	}
	
	@SuppressWarnings("unchecked")
	private int compare(JavaBeanWrapper w1, JavaBeanWrapper w2) {
		int result = 0;
		
		for (Iterator<SortField2> iterator = sortFields.iterator(); iterator.hasNext() && result == 0;) {
			SortField2 field = iterator.next();
			Expression expression = field.getExpression();
			String prop = expression.getExpressionString();
			Ordering ordering = field.getOrdering();
			if (ordering != null) {
				Object p1 = w1.getProperty(prop);
				Object p2 = w2.getProperty(prop);
				if (p1 == p2 && p1 instanceof Comparator) {
					result = ((Comparator<Object>)p1).compare(w1.getWrappedObject(), w2.getWrappedObject());
				} else if (p1 instanceof String && p2 instanceof String) {
					result = ((String)p1).trim().compareToIgnoreCase(((String)p2).trim());
				} else if (p1 instanceof Comparable && p2 instanceof Comparable) {
					result = ((Comparable<Object>) p1).compareTo(p2);
				} else if (p1 == null && p2 != null) {
					result = 1;
				} else if (p2 == null && p1 != null) {
					result = -1;
				}
				
				if (ordering.equals(Ordering.DESCENDING)) {
					result = -result;
				}
			}
			
		}
		
		return result;
	}
}