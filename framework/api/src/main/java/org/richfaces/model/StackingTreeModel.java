/**
 * 
 */
package org.richfaces.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.richfaces.model.ListRowKey.CompositeKey;
import org.w3c.dom.NamedNodeMap;

/**
 * That is intended for internal use
 * 
 * @author Nick Belaevski mailto:nbelaevski@exadel.com created 25.07.2007
 * 
 */
public class StackingTreeModel extends AbstractTreeDataModel {

	//ctor arguments
	private String id;
	private String var;
	private StackingTreeModelDataProvider dataProvider;

	//structural elements
	private StackingTreeModel parent;
	private Map<String, StackingTreeModel> models = new LinkedHashMap<String, StackingTreeModel>();

	private Object rowKey;

	private class StackEntry {
		private Object modelKey;
		private Object varObject;
		private StackingTreeModel model;
		public StackEntry(Object varObject, Object modelKey, StackingTreeModel model) {
			super();
			this.varObject = varObject;
			this.modelKey = modelKey;
			this.model = model;
		}
	}

	private Object rowData;
//	private StackingTreeModel stackingTreeModel;
	private LinkedList<StackEntry> stackEntries = new LinkedList<StackEntry>();

	public ExtendedDataModel getDataModel() {
		Object data = dataProvider.getData();
		ExtendedDataModel dataModel;
		if (data instanceof Map || data instanceof NamedNodeMap) {
			dataModel = new MapDataModel();
		} else {
			dataModel = new SequenceDataModel();
		}

		dataModel.setWrappedData(data);
		return dataModel;
	}

	protected StackingTreeModel getCurrentModel() {
		if (this.rowKey == null) {
			return this;
		}
		
		if (isRowAvailable()) {
			return ((StackEntry) stackEntries.getLast()).model;
		}

		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}
	
	public boolean isEmpty() {
		//TODO optimize that
		return getDataModel().getRowCount() == 0;
	}

	private void leaveModel(Iterator<StackEntry> iterator, StackEntry currentEntry, FacesContext context) {
		if (iterator == null) {
			return ;
		}

		LinkedList<StackEntry> stack = new LinkedList<StackEntry>();

		StackingTreeModel lastModel = null;
		if (currentEntry != null) {
			iterator.remove();
			stack.addFirst(currentEntry);
			lastModel = currentEntry.model;
		}
		
		while (iterator.hasNext()) {
			StackEntry entry = (StackEntry) iterator.next();
			if (entry.model != lastModel) {
				//always true for non-recursive models
				lastModel = entry.model;
				stack.addFirst(entry);
			}
			
			iterator.remove();
		}

		for (Iterator<StackEntry> iterator2 = stack.iterator(); iterator2.hasNext();) {
			StackEntry stackEntry = (StackEntry) iterator2.next();
			stackEntry.model.setupVariable(stackEntry.varObject, context);
		}
	}

	protected StackingTreeModel doSetupKey(Iterator<Key> keyIterator, Iterator<StackEntry> entriesIterator, FacesContext context, Object modelKey) {
		if (modelKey != null) {
			if (!setupModel(modelKey, context)) {
				//no key is available
				leaveModel(getRoot().stackEntries.iterator(), null, context);
				return null;
			}
			
			//TODO what's here?
		}
		
		if (keyIterator != null && keyIterator.hasNext()) {
			Key key = keyIterator.next();
			StackingTreeModel stackingTreeModel = this.getInternalModelById(key.modelId);
			Iterator<StackEntry> nextEntriesIterator = null;
			Object nextModelKey = key.modelKey;
			
			if (entriesIterator != null && entriesIterator.hasNext()) {
				StackEntry entry = entriesIterator.next();
				if (!entry.model.equals(stackingTreeModel) || !entry.modelKey.equals(nextModelKey)) {
					leaveModel(entriesIterator, entry, context);
				} else {
					//continue iterating entries, they still lead us by key path
					nextEntriesIterator = entriesIterator;
					nextModelKey = null;
				}
			}

			//should not be called when nextEntriesIterator & nextModelKey are both valid
			return stackingTreeModel.doSetupKey(keyIterator, nextEntriesIterator, context, nextModelKey);
		
		} else {
			leaveModel(entriesIterator, null, context);
			return this;
		}
	}
	
	protected StackingTreeModel setupKey(Object key, FacesContext context) {
		if (key == this.rowKey) {
			if (stackEntries.isEmpty()) {
				return this;
			} else {
				return (stackEntries.getLast()).model;
			}
		} else {
			Iterator<Key> keyIterator = null;
			if (key != null) {
				keyIterator = ((ListRowKey<Key>) key).iterator();
			}
			
			StackingTreeModel model = doSetupKey(keyIterator, stackEntries.iterator(), context, null);
			this.rowKey = key;

			return model;
		}
	}

	public StackingTreeModel(String id, String var, StackingTreeModelDataProvider dataProvider) {
		super();
		this.id = id;
		this.var = var;
		this.dataProvider = dataProvider;
	}

	public StackingTreeModel() {
		this(null, null, null);
	}

	private Object setupVariable(Object variable, FacesContext context) {
		if (var != null) {
			Map map = context.getExternalContext().getRequestMap();
			return map.put(var, variable);
		}

		return null;
	}

	public boolean setupModel(Object key, FacesContext facesContext) {
		ExtendedDataModel dataModel = getDataModel();
		dataModel.setRowKey(key);

		if (dataModel.isRowAvailable()) {
			Object rowData = dataModel.getRowData();
			//System.out.println("StackingTreeModel.setupModel() " + rowData);
			Object varObject = setupVariable(rowData, facesContext);

			this.rowData = rowData;
			getRoot().stackEntries.add(new StackEntry(varObject, key, this));

			return true;
		}
		
		return false;
	}

	public void setParent(StackingTreeModel parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.richfaces.model.AbstractTreeDataModel#getTreeNode()
	 */
	public TreeNode getTreeNode() {
		if (isRowAvailable()) {
			return null;
		}

		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.richfaces.model.AbstractTreeDataModel#isLeaf()
	 */
	public boolean isLeaf() {
		if (isRowAvailable()) {
			StackEntry lastEntry = (StackEntry) stackEntries.getLast();
			for (Iterator iterator = lastEntry.model.getInternalModelsIterator(); iterator.hasNext();) {
				StackingTreeModel stackingTreeModel = (StackingTreeModel) iterator.next();

				if (!stackingTreeModel.isEmpty()) {
					return false;
				}
			}

			return true;
		}
		
		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}

	protected StackingTreeModel getRoot() {
		if (parent != null) {
			return parent.getRoot();
		}

		return this;
	}

	protected void doWalk(FacesContext context, DataVisitor dataVisitor,
			Range range, ListRowKey argumentKey, Object argument,
			boolean last) throws IOException {

		TreeRange treeRange = (TreeRange) range;
		
		if (treeRange == null || treeRange.processNode(argumentKey)) {
			if (argumentKey != null) {
				getRoot().setRowKey(argumentKey);
				processElement(context, dataVisitor, argument, argumentKey, last);
				getRoot().setRowKey(argumentKey);
			} else {
				getRoot().setRowKey(argumentKey);
			}

			final ShiftingDataVisitor shiftingDataVisitor = new ShiftingDataVisitor(
					new Visitor1(dataVisitor));
			
			if (treeRange == null || treeRange.processChildren(argumentKey)) {
				Iterator iterator = this.getInternalModelsIterator();
				while (iterator.hasNext()) {
					final StackingTreeModel model = (StackingTreeModel) iterator.next();
					final ExtendedDataModel scalarModel = model.getDataModel();
					
					Argument argument2 = new Argument();
					argument2.listRowKey = argumentKey;
					argument2.argument = argument;
					// setup current model
					argument2.model = model;
					argument2.range = range;
					
					scalarModel.walk(context, new DataVisitor() {

						public void process(FacesContext context,
								Object rowKey, Object argument)
								throws IOException {

							Object key = scalarModel.getRowKey();
							scalarModel.setRowKey(rowKey);
							Object data = scalarModel.getRowData();
							
							Object variable = model.setupVariable(data, context);
							boolean activeData = model.isActiveData();
							model.setupVariable(variable, context);
							scalarModel.setRowKey(key);

							if (activeData) {
								shiftingDataVisitor.process(context, rowKey, argument);
							}
						}
						
					}, null, argument2);
					
				}
			}
			
			shiftingDataVisitor.end(context);
		}
	}

	private StackingTreeModel getInternalModelById(String id) {
		StackingTreeModel model = getModelById(id);
		if (model.isActive()) {
			return model;
		}
		
		throw new IllegalStateException();
	}
	
	public StackingTreeModel getModelById(String id) {
		return (StackingTreeModel) models.get(id);
	}
	
	private Iterator getInternalModelsIterator() {
		return new FilterIterator(getModelsIterator(), ACTIVE_MODEL_PREDICATE);
	}
	
	public Iterator getModelsIterator() {
		return models.values().iterator();
	}
	
	public void walk(FacesContext context, DataVisitor dataVisitor,
			Range range, Object rowKey, Object argument,
			boolean last) throws IOException {

		if (rowKey != null) {
			ListRowKey listRowKey = (ListRowKey) rowKey;

			StackingTreeModel treeModel = getRoot().setupKey(listRowKey, context);

			treeModel.doWalk(context, dataVisitor, range, listRowKey, argument,
					last);

		} else {
			doWalk(context, dataVisitor, range, (ListRowKey) rowKey, argument, last);
		}
	}

	private class Argument {
		private ListRowKey listRowKey;
		private StackingTreeModel model;
		private Range range;
		private Object argument;
	}

	private class Visitor1 implements DataVisitor, LastElementAware {
		private DataVisitor dataVisitor;
		private boolean theLast;

		public Visitor1(DataVisitor dataVisitor) {
			super();
			this.dataVisitor = dataVisitor;
		}

		public void process(FacesContext context, Object rowKey, Object argument)
		throws IOException {

			Argument a = (Argument) argument;
			ListRowKey listRowKey = new ListRowKey(a.listRowKey, new Key(
					a.model.id, rowKey));
			//System.out.println(".walk() " + (theLast ? " * " : "") + listRowKey);

			a.model.doWalk(context, dataVisitor, a.range, listRowKey, a.argument,
					theLast);
		}

		public void resetLastElement() {
			theLast = false;
		}

		public void setLastElement() {
			theLast = true;
		}

	}

	private static class ShiftingDataVisitor implements DataVisitor {

		private DataVisitor dataVisitor;

		public ShiftingDataVisitor(DataVisitor dataVisitor) {
			super();
			this.dataVisitor = dataVisitor;
		}

		private Object rowKey;
		private Object argument;
		private boolean shifted = false;

		public void process(FacesContext context, Object rowKey, Object argument)
		throws IOException {

			if (!shifted) {
				this.rowKey = rowKey;
				this.argument = argument;
				this.shifted = true;
			} else {
				dataVisitor.process(context, this.rowKey, this.argument);
				this.rowKey = rowKey;
				this.argument = argument;
			}
		}

		public void end(FacesContext context) throws IOException {
			if (shifted) {
				try {
					((LastElementAware) dataVisitor).setLastElement();
					dataVisitor.process(context, this.rowKey, argument);
				} finally {
					((LastElementAware) dataVisitor).resetLastElement();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.richfaces.model.AbstractTreeDataModel#walkModel(javax.faces.context.FacesContext,
	 *      org.ajax4jsf.model.DataVisitor, org.ajax4jsf.model.Range,
	 *      java.lang.Object, java.lang.Object, boolean)
	 */
	public void walkModel(FacesContext facesContext, DataVisitor visitor,
			Range range, Object key, Object argument, boolean last)
	throws IOException {

		walk(facesContext, visitor, range, key, argument, last);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	public Object getRowKey() {
		return rowKey;
	}

	public void setRowKey(Object key) {
		setupKey(key, FacesContext.getCurrentInstance());
	}

	public void addStackingModel(StackingTreeModel model) {
		this.models.put(model.id, model);
		model.setParent(this);
	}

	public void removeStackingModel(StackingTreeModel model) {
		this.models.remove(model.id);
		model.setParent(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	public Object getRowData() {
		if (isRowAvailable()) {
			StackEntry lastEntry = (StackEntry) stackEntries.getLast();
			return lastEntry.model.rowData;
		}
		
		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	public boolean isRowAvailable() {
		return !stackEntries.isEmpty();
	}

	public StackingTreeModel getParent() {
		return parent;
	}

	/**
	 * That is intended for internal use
	 * 
	 * @author Nick Belaevski
	 */
	protected static class Key implements Serializable, CompositeKey {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6821854350257816571L;
		protected Object modelKey;
		protected String modelId;

		public Key(String modelId, Object modelKey) {
			super();
			this.modelId = modelId;
			this.modelKey = modelKey;
		}

		public String toString() {
			return this.modelId + ":" + this.modelKey;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((modelId == null) ? 0 : modelId.hashCode());
			result = prime * result
					+ ((modelKey == null) ? 0 : modelKey.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Key other = (Key) obj;
			if (modelId == null) {
				if (other.modelId != null)
					return false;
			} else if (!modelId.equals(other.modelId))
				return false;
			if (modelKey == null) {
				if (other.modelKey != null)
					return false;
			} else if (!modelKey.equals(other.modelKey))
				return false;
			return true;
		}

		static enum IteratorState {
		    INITIAL {
			@Override
			protected boolean hasNext(Key arg0) {
			    return true;
			}

			@Override
			protected Object next(Key arg0) {
			    throw new IllegalStateException();
			}
			
			@Override
			protected IteratorState nextState() {
			    return ID;
			}
		    },
		    
		    ID {
			@Override
			protected boolean hasNext(Key arg0) {
			    return true;
			}

			@Override
			protected Object next(Key arg0) {
			    return arg0.modelId;
			}
			
			@Override
			protected IteratorState nextState() {
			    return KEY;
			}
		    },
		    
		    KEY {
			@Override
			protected boolean hasNext(Key arg0) {
			    return false;
			}

			@Override
			protected Object next(Key arg0) {
			    return arg0.modelKey;
			}
			
			@Override
			protected IteratorState nextState() {
			    return DONE;
			}
		    },
		    
		    DONE {
			@Override
			protected boolean hasNext(Key arg0) {
			    return false;
			}

			@Override
			protected Object next(Key arg0) {
			    throw new NoSuchElementException();
			}
			
			@Override
			protected IteratorState nextState() {
			    return DONE;
			}
		    };
		    
		    protected abstract boolean hasNext(Key key);

		    protected abstract Object next(Key key);

		    protected abstract IteratorState nextState();
		};

		public Iterator getKeySegments() {
		    
		    return new Iterator<Object>() {
			IteratorState state = IteratorState.INITIAL;

			public boolean hasNext() {
			    return state.hasNext(Key.this);
			}

			public Object next() {
			    state = state.nextState();
			    return state.next(Key.this);
			}

			public void remove() {
			    throw new UnsupportedOperationException();
			}

		    };
		}
	}

	protected boolean isActiveData() {
		return true;
	}
	
	protected boolean isActive() {
		return true;
	}

	private final static Predicate ACTIVE_MODEL_PREDICATE = new Predicate() {

		public boolean evaluate(Object object) {
			StackingTreeModel model = (StackingTreeModel) object;
			if (model == null) {
				return false;
			}

			return model.isActive();
		}
		
	};

	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWrappedData(Object data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object convertToKey(FacesContext context, String keyString, 
		UIComponent component, Converter converter) {

	    //force model leave
	    setRowKey(null);
	    
	    String[] strings = ListRowKey.fromString(keyString);
	    int l = strings.length / 2;
	    List<Object> list = new ArrayList<Object>(l);
	    StackingTreeModel model = getRoot();
	    
	    for (int i = 0; i < l; i++) {
		int idx = i*2;

		String modelId = strings[idx];
		model = model.getModelById(modelId);
		if (model.isActive()) {
			Object key = model.convert(context, strings[idx + 1], component, converter);
			if (key == null) {
			    return null;
			}
			
			list.add(new Key(modelId, key));
			
			if (!model.setupModel(key, context) || !model.isActiveData()) {
			    return null;
			}
		} else {
		    return null;
		}
	    }
	    
	    return new ListRowKey<Object>(list);
	}

	protected Object convert(FacesContext context, String string, 
		UIComponent component, Converter converter) {
	    
	    ConvertableKeyModel convertable = (ConvertableKeyModel) getDataModel();
	    return convertable.getKeyAsObject(context, string, component, converter);
	}
}
