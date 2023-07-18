package org.richfaces.model.impl;

import java.util.Comparator;

import org.richfaces.model.ScrollableTableDataModel;
import org.richfaces.model.SortOrder;

public abstract class SimpleGridDataModel extends ScrollableTableDataModel {

	protected Comparator createComparator(SortOrder sortOrder) {
		return new PropertyResolverComparator(sortOrder);
	}

}
