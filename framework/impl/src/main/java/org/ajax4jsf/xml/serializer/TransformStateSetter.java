/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
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

/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: TransformStateSetter.java,v 1.1.2.1 2007/01/09 18:57:27 alexsmirnov Exp $
 */
package org.ajax4jsf.xml.serializer;

import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
/**
 * This interface is meant to be used by a base interface to
 * TransformState, but which as only the setters which have non Xalan
 * specific types in their signature, so that there are no dependancies
 * of the serializer on Xalan.
 * 
 * This interface is not a public API, it is only public because it is
 * used by Xalan.
 * 
 * @see org.apache.xalan.transformer.TransformState
 * @xsl.usage internal
 */
public interface TransformStateSetter
{


  /**
   * Set the current node.
   *
   * @param n The current node.
   */
  void setCurrentNode(Node n);

  /**
   * Reset the state on the given transformer object.
   *
   * @param transformer
   */
  void resetState(Transformer transformer);

}
