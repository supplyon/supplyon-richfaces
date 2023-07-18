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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;


/**
 * Interface representing tree ADT nodes
 * @author Nick Belaevski - nbelaevski@exadel.com
 * created 16.11.2006
 */
public interface TreeNode<T> extends Serializable{
	
	/**
	 * getter for node attached data
	 * @return node attached data
	 */
	public T getData();
	/**
	 * setter for node attached data
	 * @param data data to set as attached node data
	 */
	public void setData(T data);

	/**
	 * Returns whether this node is leaf
	 * @return <code>true</code> if this node is leaf else returns <code>false</code>
	 */
	public boolean isLeaf();
	
	/**
	 * getter for children
	 * @return {@link Iterator} of {@link Map.Entry} instances containing {@link TreeNode} as values
	 * and their identifiers as keys
	 */
	public Iterator<Map.Entry<Object, TreeNode<T>>> getChildren();
	/**
	 * find child by id
	 * @param id identifier of the child to find
	 * @return designated {@link TreeNode} instance or <code>null</code>
	 */
	public TreeNode<T> getChild(Object id);

	/**
	 * adds child to children collection
	 * @param identifier child identifier
	 * @param child child
	 */
	public void addChild(Object identifier, TreeNode<T> child);

	/**
	 * removes child from children collection by child id 
	 * @param id id of the child to remove
	 */
	public void removeChild(Object id);

	/**
	 * getter for parent {@link TreeNode}
	 * @return parent {@link TreeNode} instance or <code>null</code> if this node is root
	 */
	public TreeNode<T> getParent();
	/**
	 * setter for parent {@link TreeNode}
	 * @param parent {@link TreeNode} to set as parent
	 */
	public void setParent(TreeNode<T> parent);
}
