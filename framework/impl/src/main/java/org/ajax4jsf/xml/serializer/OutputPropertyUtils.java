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
 * $Id: OutputPropertyUtils.java,v 1.1.2.1 2007/01/09 18:57:15 alexsmirnov Exp $
 */
package org.ajax4jsf.xml.serializer;

import java.util.Properties;

/**
 * This class contains some static methods that act as helpers when parsing a
 * Java Property object.
 * 
 * This class is not a public API. 
 * It is only public because it is used outside of this package.
 * 
 * @see java.util.Properties
 * @xsl.usage internal
 */
public final class OutputPropertyUtils
{
    /**
      * Searches for the boolean property with the specified key in the property list.
      * If the key is not found in this property list, the default property list,
      * and its defaults, recursively, are then checked. The method returns
      * <code>false</code> if the property is not found, or if the value is other
      * than "yes".
      *
      * @param   key   the property key.
      * @param   props   the list of properties that will be searched.
      * @return  the value in this property list as a boolean value, or false
      * if null or not "yes".
      */
    public static boolean getBooleanProperty(String key, Properties props)
    {

        String s = props.getProperty(key);

        if (null == s || !s.equals("yes"))
            return false;
        else
            return true;
    }

    /**
     * Searches for the int property with the specified key in the property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>false</code> if the property is not found, or if the value is other
     * than "yes".
     *
     * @param   key   the property key.
     * @param   props   the list of properties that will be searched.
     * @return  the value in this property list as a int value, or 0
     * if null or not a number.
     */
    public static int getIntProperty(String key, Properties props)
    {

        String s = props.getProperty(key);

        if (null == s)
            return 0;
        else
            return Integer.parseInt(s);
    }

}
