/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jitb.lang;

import jitb.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.*;
import java.lang.Object;
import java.lang.String;

/**
 * @author Joa Ebert
 */
public final class XML extends jitb.lang.Object {
	public static XML JITB$fromDocument(final Document value) {
		return new XML(value);
	}

	private final Document _document;

	public XML(final jitb.lang.Object value) {
		if(value instanceof jitb.lang.String) {
			try {
				_document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(((jitb.lang.String)value).toString())));
			} catch(final Throwable t) {
				//todo throw flash error
				throw new RuntimeException(t);
			}
		} else {
			//todo other types as in as3 langref
			throw new RuntimeException("unsupported "+value);
		}
	}

	protected XML(final Document value) {
		_document = value;
	}

	@Override
	public java.lang.String toString() {
		return toXMLString();
	}

	public java.lang.String toXMLString() {
		return XMLUtil.toXMLString(_document);
	}

	@Override
	public Object JITB$getProperty(final String property) {
		return new XMLList(_document.getElementsByTagName(property));
	}
}
