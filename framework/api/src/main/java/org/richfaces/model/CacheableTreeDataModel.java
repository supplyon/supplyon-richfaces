/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
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

package org.richfaces.model;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;

/**
 * Extension of {@link TreeDataModel} supporting lazy data fetching for caching
 * 
 * created 08.01.2007
 * 
 * @author Nick - mailto:nbelaevski@exadel.com 
 */
public abstract class CacheableTreeDataModel<T> extends TreeDataModel<T> {

	private final class Visitor implements DataVisitor, LastElementAware {
		private final DataVisitor visitor;

		private Visitor(DataVisitor visitor) {
			this.visitor = visitor;
		}

		public void process(FacesContext context, Object rowKey, Object argument)
				throws IOException {
			TreeRowKey treeRowKey = (TreeRowKey) rowKey;
			treeDataModel.setRowKey(treeRowKey);
			setDefaultNodeData(locateTreeNode(treeRowKey, true), treeDataModel.getRowData());

			if (visitor != null) {
				visitor.process(context, rowKey, argument);
			}
		}

		public void resetLastElement() {
			if (visitor instanceof LastElementAware) {
				((LastElementAware) visitor).resetLastElement();
				
			}
		}

		public void setLastElement() {
			if (visitor instanceof LastElementAware) {
				((LastElementAware) visitor).setLastElement();
				
			}
		}
	}

	private final static DataVisitor NULL_VISITOR = new DataVisitor() {

	    public void process(FacesContext context, Object rowKey, Object argument) throws IOException {
		//do nothing
	    }
	    
	};
	
	private TreeDataModel<T> treeDataModel;

	public boolean isLeaf() {
		TreeRowKey rowKey = (TreeRowKey) getRowKey();
		T treeNode = locateTreeNode(rowKey);
		if (treeNode != null && !nodeAdaptor.isLeaf(treeNode)) {
			return false;
		}
			
		treeNode = treeDataModel.locateTreeNode(rowKey);
		if (treeNode != null) {
			return nodeAdaptor.isLeaf(treeNode);
		}

		return false;
	}

	public CacheableTreeDataModel(TreeDataModel<T> model, MissingNodeHandler<T> missingNodeHandler) {
		super(model.getClazz(), model.getNodeAdaptor(), missingNodeHandler);
		setWrappedData(missingNodeHandler.handleMissingNode(null, null));
		setTreeDataModel(model);
	}

	public void walkModel(FacesContext context, DataVisitor visitor,
			Range range, Object key, Object argument, boolean last)
			throws IOException {
		treeDataModel.walkModel(context, new Visitor(visitor), range, key,
				argument, last);
	}

	public void setTreeDataModel(TreeDataModel<T> treeDataModel) {
		this.treeDataModel = treeDataModel;
	}
	
	public TreeDataModel<T> getTreeDataModel() {
		return treeDataModel;
	}

	public void walk(FacesContext context, final DataVisitor dataVisitor,
			Range range, Object rowKey, Object argument, boolean last)
			throws IOException {
		
		T cachedTreeNode = locateTreeNode((TreeRowKey) rowKey);
		T treeNode = treeDataModel.locateTreeNode((TreeRowKey) rowKey);
		
		if (treeNode != null) {
			if (cachedTreeNode == null || (nodeAdaptor.isLeaf(cachedTreeNode) && !nodeAdaptor.isLeaf(treeNode))) {
				//fill cache
				treeDataModel.walk(context, new Visitor(dataVisitor), range,
						rowKey, argument, last);
			} else {
				super.walk(context, dataVisitor, range, rowKey, argument, last);
			}
		}
	}

	public void setTransient(boolean newTransientValue) {
		if (!newTransientValue) {
			throw new IllegalArgumentException(
					"ReplaceableTreeDataModel shouldn't be transient!");
		}
	}
	
	protected abstract void setDefaultNodeData(T node, Object data);

	@Override
	public Object convertToKey(FacesContext context, String keyString, UIComponent component, Converter converter) {
	Object convertedKey = treeDataModel.convertToKey(context, keyString, component, converter);

	if (convertedKey != null) {
	    final TreeRowKey treeRowKey = (TreeRowKey) convertedKey;
	    try {
		walk(context, NULL_VISITOR, new TreeRange() {

		public boolean processChildren(TreeRowKey rowKey) {
		    return rowKey == null || rowKey.isSubKey(treeRowKey);
		}

		public boolean processNode(TreeRowKey rowKey) {
		    return this.processChildren(rowKey) || rowKey.equals(treeRowKey);
		}
		
		}, null);
	    } catch (IOException e) {
		context.getExternalContext().log(e.getLocalizedMessage(), e);
		
		return null;
	    }
	}
	
	return convertedKey;
	}
}
