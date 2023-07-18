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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.ajax4jsf.util.base64;

/**
 * <p>Provides the highest level of abstraction for Decoders.
 * This is the sister interface of {@link Encoder}.  All
 * Decoders implement this common generic interface.</p>
 * 
 * <p>Allows a user to pass a generic Object to any Decoder 
 * implementation in the codec package.</p>
 * 
 * <p>One of the two interfaces at the center of the codec package.</p>
 * 
 * @author Apache Software Foundation
 * @version $Id: Decoder.java,v 1.1.2.1 2007/01/09 18:59:14 alexsmirnov Exp $
 */
public interface Decoder {

    /**
     * Decodes an "encoded" Object and returns a "decoded"
     * Object.  Note that the implementation of this
     * interface will try to cast the Object parameter
     * to the specific type expected by a particular Decoder
     * implementation.  If a {@link java.lang.ClassCastException} occurs
     * this decode method will throw a DecoderException.
     * 
     * @param pObject an object to "decode"
     * 
     * @return a 'decoded" object
     * 
     * @throws DecoderException a decoder exception can
     * be thrown for any number of reasons.  Some good
     * candidates are that the parameter passed to this
     * method is null, a param cannot be cast to the
     * appropriate type for a specific encoder.
     */
    Object decode(Object pObject) throws DecoderException;
}  

