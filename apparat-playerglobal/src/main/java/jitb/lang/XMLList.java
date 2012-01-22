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
import org.w3c.dom.NodeList;

import java.lang.*;

/**
 * @author Joa Ebert
 */
public final class XMLList extends jitb.lang.Object {
	private final NodeList _nodeList;

	protected XMLList(final NodeList value) {
		_nodeList = value;
	}

	@Override
	public java.lang.String toString() {
		return toXMLString();
	}

	public java.lang.String toXMLString() {
		final StringBuffer buffer = new StringBuffer();

		final int n = _nodeList.getLength();
		final int m = n - 1;

		for(int i = 0; i < n; ++i) {
			buffer.append(XMLUtil.toXMLString(_nodeList.item(i)));

			if(i != m) {
				buffer.append('\n');
			}
		}

		return buffer.toString();
	}
}
