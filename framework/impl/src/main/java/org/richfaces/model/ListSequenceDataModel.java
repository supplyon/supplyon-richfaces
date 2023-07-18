/**
 * 
 */
package org.richfaces.model;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;

/**
 * @author Konstantin Mishin
 *
 */
public class ListSequenceDataModel extends ExtendedDataModel {
	
	private List<?> list;
	private int index;

	public ListSequenceDataModel(List<?> list) {
		setWrappedData(list);
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		if(index<0){
			return null;
		}
		return new Integer(index);
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object key) {
		if(null == key){
			index = -1;
		} else {
			setRowIndex(((Integer) key).intValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#walk(javax.faces.context.FacesContext, org.ajax4jsf.model.DataVisitor, org.ajax4jsf.model.Range, java.lang.Object)
	 */
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range,
			Object argument) throws IOException {
		final SequenceRange seqRange = (SequenceRange) range;
		int rows = seqRange.getRows();
		int rowCount = list !=null ? list.size() : -1;
		int currentRow = seqRange.getFirstRow();
		if(rows > 0){
			rows += currentRow;
			rows = Math.min(rows, rowCount);
		} else {
			rows = rowCount;
		}
		for (; currentRow < rows; currentRow++) {
			setRowIndex(currentRow);
			visitor.process(context, new Integer(currentRow), argument);
		}
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (list == null) {
			return (-1);
		}
		return list.size();
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public Object getRowData() {
		if (!isRowAvailable()) {
			throw new IllegalArgumentException();
		} else {
        	return list.get(index);
        }
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowIndex()
	 */
	@Override
	public int getRowIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		return list;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		return list != null && 0 <= index && index < list.size();
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#setRowIndex(int)
	 */
	@Override
	public void setRowIndex(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException();
        }
        index = rowIndex;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	@Override
	public void setWrappedData(Object data) {
        if (data == null) {
            list = null;
            setRowIndex(-1);
        } else {
            list = (List<?>) data;
            setRowIndex(0);
        }
	}
}
