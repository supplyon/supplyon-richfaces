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
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: SerializerConstants.java,v 1.1.2.1 2007/01/09 18:57:21 alexsmirnov Exp $
 */
package org.ajax4jsf.xml.serializer;

/**
 * Constants used in serialization, such as the string "xmlns"
 * @xsl.usage internal
 */
interface SerializerConstants
{

    /** To insert ]]> in a CDATA section by ending the last CDATA section with
     * ]] and starting the next CDATA section with >
     */
    static final String CDATA_CONTINUE = "]]]]><![CDATA[>";
    /**
     * The constant "]]>"
     */
    static final String CDATA_DELIMITER_CLOSE = "]]>";
    static final String CDATA_DELIMITER_OPEN = "<![CDATA[";

    static final String EMPTYSTRING = "";

    static final String ENTITY_AMP = "&amp;";
    static final String ENTITY_CRLF = "&#xA;";
    static final String ENTITY_GT = "&gt;";
    static final String ENTITY_LT = "&lt;";
    static final String ENTITY_QUOT = "&quot;";

    static final String XML_PREFIX = "xml";
    static final String XMLNS_PREFIX = "xmlns";
    static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
   
    public static final String DEFAULT_SAX_SERIALIZER="org.ajax4jsf.xml.serializer.ToXMLSAXHandler";
    
    /**
     * Define the XML version.
     */
    static final String XMLVERSION11 = "1.1";
    static final String XMLVERSION10 = "1.0";
}
