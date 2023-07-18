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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.TreeNode;

/**
 * {@link TreeDataModel} adaptor for generic types. Contains adaptors for Swing & classic ({@link TreeNode}) 
 * tree node types embedded into this interface as static instances
 * 
 * Created 01.11.2007
 * 
 * @author Nick Belaevski
 * @since 3.2
 */

public interface TreeDataModelNodeAdaptor<T> {
	public T getParent(T node);
	
	public boolean isLeaf(T node);
	
	public T getChild(T node, Object key);

	public Iterator<Map.Entry<Object, T>> getChildren(T node);
	
	public Object getRowData(T node);

	/**
	 * Instance of {@link TreeDataModelNodeAdaptor} for {@link org.richfaces.model.TreeNode} nodes handling
	 */
	@SuppressWarnings("unchecked")
	public static final TreeDataModelNodeAdaptor<org.richfaces.model.TreeNode> classicTreeNodeAdaptor = 
		new TreeDataModelNodeAdaptor<org.richfaces.model.TreeNode>() {

			public org.richfaces.model.TreeNode getChild(org.richfaces.model.TreeNode node, Object key) {
				return node.getChild(key);
			}

			public Iterator<Entry<Object, org.richfaces.model.TreeNode>> getChildren(
					org.richfaces.model.TreeNode node) {
				return node.getChildren();
			}

			public org.richfaces.model.TreeNode getParent(org.richfaces.model.TreeNode node) {
				return node.getParent();
			}

			public Object getRowData(org.richfaces.model.TreeNode node) {
				return node.getData();
			}

			public boolean isLeaf(org.richfaces.model.TreeNode node) {
				return node.isLeaf();
			}

	};

	/**
	 * Instance of {@link TreeDataModelNodeAdaptor} for {@link javax.swing.tree.TreeNode} nodes handling
	 */
	public static final TreeDataModelNodeAdaptor<javax.swing.tree.TreeNode> swingTreeNodeAdaptor = 
		new TreeDataModelNodeAdaptor<javax.swing.tree.TreeNode>() {

			final class SwingNodeMapEntry implements Map.Entry<Object, TreeNode> {

				private Object key;
				private TreeNode child;
				
				public SwingNodeMapEntry(int i, TreeNode child) {
					this.key = Integer.valueOf(i);
					this.child = child;
				}

				public Object getKey() {
					return this.key;
				}

				public TreeNode getValue() {
					return child;
				}

				public TreeNode setValue(TreeNode value) {
					TreeNode node = this.child;
					this.child = value;

					return node;
				}
				
			};
		
			public javax.swing.tree.TreeNode getChild(
					javax.swing.tree.TreeNode node, Object key) {

				int intKey = ((Integer) key).intValue();
				if (intKey < node.getChildCount() && intKey >= 0) {
					return node.getChildAt(intKey);
				}

				return null;
			}

			public Iterator<Entry<Object, javax.swing.tree.TreeNode>> getChildren(
					final javax.swing.tree.TreeNode node) {
				
				if (node.getAllowsChildren()) {
					return new Iterator<Entry<Object,javax.swing.tree.TreeNode>>() {

						private final Enumeration<?> e = node.children();
						private int counter = 0;
						
						public boolean hasNext() {
							return e.hasMoreElements();
						}

						public Entry<Object, javax.swing.tree.TreeNode> next() {
							javax.swing.tree.TreeNode child = (javax.swing.tree.TreeNode) e.nextElement();
							SwingNodeMapEntry entry = new SwingNodeMapEntry(counter++, child);
							return entry;
							
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}
						
					};
				}
				
				return null;
			}

			public javax.swing.tree.TreeNode getParent(
					javax.swing.tree.TreeNode node) {
				return node.getParent();
			}

			public Object getRowData(javax.swing.tree.TreeNode node) {
				return node;
			}

			public boolean isLeaf(javax.swing.tree.TreeNode node) {
				return !node.getAllowsChildren() || node.isLeaf();
			}
	};
}
