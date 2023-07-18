/**
 * 
 */
package org.richfaces.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModelListener;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.richfaces.model.impl.expressive.JavaBeanWrapper;
import org.richfaces.model.impl.expressive.ObjectWrapperFactory;
import org.richfaces.model.impl.expressive.WrappedBeanComparator2;
import org.richfaces.model.impl.expressive.WrappedBeanFilter;

/**
 * @author Konstantin Mishin
 *
 */
public class ModifiableModel extends ExtendedDataModel{
	
	private static final Log log = LogFactory.getLog(ModifiableModel.class);

	private ExtendedDataModel delegate;
	
	private ExtendedDataModel originalModel;
	
	private ExtendedDataModel modifiedModel;

	private String var;
	
	private List<FilterField> filterFields;
	private List<SortField2> sortFields;
	
	public ModifiableModel(ExtendedDataModel originalModel, String var,
			List<FilterField> filterFields, List<SortField2> sortFields) {
		this.originalModel = originalModel;
		delegate = originalModel;
		this.var = var;
		this.filterFields = filterFields;
		this.sortFields = sortFields;
	}
	
	public void addDataModelListener(DataModelListener listener) {
		originalModel.addDataModelListener(listener);
	}

	public DataModelListener[] getDataModelListeners() {
		return originalModel.getDataModelListeners();
	}

	public int getRowCount() {
		return delegate.getRowCount();
	}

	public Object getRowData() {
		return delegate.getRowData();
	}

	public int getRowIndex() {
		return delegate.getRowIndex();
	}

	public Object getRowKey() {
		return delegate.getRowKey();
	}

	public SerializableDataModel getSerializableModel(Range range) {
		return delegate.getSerializableModel(range);
	}

	public Object getWrappedData() {
		return delegate.getWrappedData();
	}

	public boolean isRowAvailable() {
		return delegate.isRowAvailable();
	}

	public void removeDataModelListener(DataModelListener listener) {
		delegate.removeDataModelListener(listener);
	}

	public void setRowIndex(int rowIndex) {
		delegate.setRowIndex(rowIndex);
	}

	public void setRowKey(Object key) {
		delegate.setRowKey(key);
	}

	public void setWrappedData(Object data) {
		delegate.setWrappedData(data);
	}

	public void walk(FacesContext context, DataVisitor visitor, Range range,
			Object argument) throws IOException {
		
		if (shouldSort() || shouldFilter()) {
			if (modifiedModel == null) {
				modifiedModel = new ListSequenceDataModel(prepareCollection());
			}
			delegate = modifiedModel;
		} else {
			delegate = originalModel;
		}
		
		delegate.walk(context, visitor, range, argument);
	}
	
	private boolean shouldSort() {
		return sortFields != null && !sortFields.isEmpty();				
	}
	
	private boolean shouldFilter() {
		return filterFields != null && !filterFields.isEmpty();				
	}
	
	private List<?> prepareCollection() {
		int rowCount = originalModel.getRowCount();
		final List<Object> collection;
		
		if (rowCount > 0) {
			collection = new ArrayList<Object>(rowCount);
		} else {
			collection = new ArrayList<Object>();
		}
		
		FacesContext context = FacesContext.getCurrentInstance();
		try {
		
			originalModel.walk(context, new DataVisitor() {
				public void process(FacesContext context, Object rowKey,
						Object argument) throws IOException {
					originalModel.setRowKey(rowKey);
					if (originalModel.isRowAvailable()) {
						collection.add(originalModel.getRowData());
					}
				}
			}, new SequenceRange(0, -1),
			null);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		List<Object> modifedcollection = collection;
		
		if (shouldFilter()) {
			List <Object> filteredCollection = new ArrayList<Object>();
			ObjectWrapperFactory wrapperFactory = new ObjectWrapperFactory(
					context, var, filterFields);
			WrappedBeanFilter wrappedBeanFilter = new WrappedBeanFilter(filterFields);
			wrapperFactory.wrapList(modifedcollection);
			for (Object object : modifedcollection) {
				if(wrappedBeanFilter.accept((JavaBeanWrapper)object)) {
					filteredCollection.add(object);
				}
			}
			modifedcollection = filteredCollection;
			wrapperFactory.unwrapList(modifedcollection);
		}

		if (shouldSort()) {
			ObjectWrapperFactory wrapperFactory = new ObjectWrapperFactory(
					context, var, sortFields);
			WrappedBeanComparator2 wrappedBeanComparator = new WrappedBeanComparator2(
					sortFields);
			wrapperFactory.wrapList(modifedcollection);
			Collections.sort(modifedcollection, wrappedBeanComparator);
			wrapperFactory.unwrapList(modifedcollection);
		}
		return modifedcollection;
 		
	}
}
