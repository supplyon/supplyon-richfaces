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
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;

/**
 * That is intended for internal use
 * 
 * @author Nick Belaevski - nbelaevski@exadel.com created 16.11.2006
 * 
 */
public abstract class TreeDataModel<T> extends AbstractTreeDataModel {
	
	private Object wrappedData;
	private Class<T> clazz;
	
	private TreeRowKey currentRowKey;

	private TreeRowKey oldRowKey;

	private Boolean rowAvailable = Boolean.FALSE;
	private T rowTreeData;

	protected final TreeDataModelNodeAdaptor<T> nodeAdaptor;

	private MissingNodeHandler<T> missingNodeHandler;
	
	/**
	 * Interface aimed to handle missing nodes for externally-generated keys. 
	 * Typical usage for the interface is filling model cache
	 * @param <T> generic tree node type
	 * 
	 * @author Nick Belaevski
	 * @since 3.2
	 */
	public static interface MissingNodeHandler<T> {
		T handleMissingNode(T parentNode, Object pathSegment);
	};
	
	public TreeDataModel(Class<T> clazz, TreeDataModelNodeAdaptor<T> nodeAdaptor, 
			MissingNodeHandler<T> missingNodeHandler) {
		
		this.clazz = clazz;
		this.nodeAdaptor = nodeAdaptor;
		this.missingNodeHandler = missingNodeHandler;
	}

	public final Class<T> getClazz() {
		return clazz;
	}
	
	public final TreeDataModelNodeAdaptor<T> getNodeAdaptor() {
		return nodeAdaptor;
	}
	
	public Object getRowKey() {
		return this.currentRowKey;
	}

	public void setRowKey(Object rowKey) {
		if (rowKey != null) {
			ListRowKey newRowKey = (ListRowKey) rowKey;
			this.currentRowKey = newRowKey;
			this.rowAvailable = null;
		} else {
			this.currentRowKey = null;
			this.oldRowKey = null;
			this.rowTreeData = null;
			this.rowAvailable = Boolean.FALSE;
		}
	}

	protected void doWalk(FacesContext context, DataVisitor dataVisitor,
			Range range, Object rowKey, Object argument, boolean last) throws IOException {
		ListRowKey listRowKey = (ListRowKey) rowKey;

		T node = locateTreeNode(listRowKey);

		if (node != null) {
			TreeRange treeRange = (TreeRange) range;

			if (treeRange == null || treeRange.processNode(listRowKey)) {

				if (nodeAdaptor.getParent(node) != null) {
					processElement(context, dataVisitor, argument, listRowKey, last);
				}

				if (treeRange == null || treeRange.processChildren(listRowKey)) {
					if (!nodeAdaptor.isLeaf(node)) {
						Iterator<Map.Entry<Object, T>> children = nodeAdaptor.getChildren(node);

						if (children != null) {
							Map.Entry<Object, T> childEntry = children.hasNext() ? children.next() : null;
							T childNode;
							Object identifier;

							if (childEntry != null) {
								childNode = childEntry.getValue();
								identifier = childEntry.getKey();
							} else {
								childNode = null;
								identifier = null;
							}

							do {
								Map.Entry<Object, T> nextChildEntry = children.hasNext() ? children.next() : null;
								T nextChildNode;
								Object nextIdentifier;

								if (nextChildEntry != null) {
									//TODO consider lazy initialization of value
								    	nextChildNode = nextChildEntry.getValue();
									nextIdentifier = nextChildEntry.getKey();
								} else {
									nextChildNode = null;
									nextIdentifier = null;
								}

								if (childNode != null) {

									boolean isLast = nextChildNode == null;

									ListRowKey newRowKey;
									if (rowKey != null) {						
										newRowKey = new ListRowKey(listRowKey, identifier);						
									} else {						
										newRowKey = new ListRowKey(identifier);						
									}

									this.doWalk(context, dataVisitor, range, newRowKey, argument, isLast);
								}

								identifier = nextIdentifier;
								childNode = nextChildNode;
							} while (childNode != null);
						}
					}
				}
			}
		}
	}
	
	public void walk(FacesContext context, DataVisitor dataVisitor,
			Range range, Object rowKey, Object argument, boolean last) throws IOException {

		if (rowKey != null) {
			setRowKey(rowKey);
			if (!isRowAvailable()) {
				throw new IllegalStateException(
						"No tree element available or row key not set!");
			}
		}
		
		doWalk(context, dataVisitor, range, rowKey, argument, last);
	}

	public T locateTreeNode(TreeRowKey rowKey) {
		return locateTreeNode(rowKey, false);
	}

	public T locateTreeNode(TreeRowKey rowKey, boolean allowCreate) {
		boolean useCached = (rowTreeData != null && rowKey != null && rowKey.equals(this.oldRowKey));
		if (!useCached) {
			T rootNode = getData();

			if (rootNode != null) {
				if (rowKey != null) {
					int commonPathLength = rowKey.getCommonPathLength(oldRowKey);
					if (oldRowKey == null) {
						rowTreeData = rootNode;
					} else {
						int rootOpsCount = rowKey.depth();
						int currentUpOpsCount = oldRowKey.depth() - commonPathLength;
						int currentOpsCount = currentUpOpsCount + rootOpsCount - commonPathLength;

						if (rootOpsCount > currentOpsCount) {
							for (int i = 0; i < oldRowKey.depth() 
							- commonPathLength; i++) {

								rowTreeData = nodeAdaptor.getParent(rowTreeData);
							}
						} else {
							commonPathLength = 0;
							rowTreeData = rootNode;
							oldRowKey = null;
						}
					}
					oldRowKey = rowKey;
					Iterator<?> iterator = rowKey.getSubPathIterator(commonPathLength);
					while (iterator.hasNext()) {
						//TODO nick - check rowTreeData for null
						
						Object pathSegment = iterator.next();
						T childRowTreeData = nodeAdaptor.getChild(rowTreeData, pathSegment);

						if (childRowTreeData == null) {
							if (!allowCreate) {
								//TODO nick - reset rowTreeData
								return null;
							} else {
								if (missingNodeHandler != null) {
									childRowTreeData = missingNodeHandler.
										handleMissingNode(rowTreeData, pathSegment);

									if (childRowTreeData == null) {
										return null;
									}
								} else {
									return null;
								}
							}
						}

						rowTreeData = childRowTreeData;
					}
				} else {
					return rootNode;
				}
			} else {
				return null;
			}
		}
		return rowTreeData;
	}

	public boolean isRowAvailable() {
		if (Boolean.FALSE.equals(rowAvailable)) {
			return false;
		}
		
		T data = locateTreeNode(this.currentRowKey);

		if (data != null) {
			return true;
		}

		return false;
	}

	public Object getRowData() {
		if (isRowAvailable()) {
			T treeNode = locateTreeNode(this.currentRowKey);
			if (treeNode != null) {
				return nodeAdaptor.getRowData(treeNode);
			}

			return null;
		}
		

		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}

	public boolean isLeaf() {
		if (isRowAvailable()) {
			T treeNode = locateTreeNode(this.currentRowKey);
			if (treeNode != null) {
				return nodeAdaptor.isLeaf(treeNode);
			}
		}

		throw new IllegalStateException(
				"No tree element available or row key not set!");
	}

	public void walkModel(FacesContext context, DataVisitor visitor, Range range, Object key, Object argument, boolean last) throws IOException {
		walk(context, visitor, range, key, argument, last);
	}

	@Override
	public Object getWrappedData() {
		return wrappedData;
	}

	@Override
	public void setWrappedData(Object data) {
		this.wrappedData = data;
	}
	
	protected T getData() {
		return clazz.cast(wrappedData);
	}
	
	public TreeNode<T> getTreeNode() {
		return null;
	}

}
