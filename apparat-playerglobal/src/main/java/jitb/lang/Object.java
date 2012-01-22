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

import jitb.errors.DynamicCodeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * @author Joa Ebert
 */
public class Object {
	public void setPropertyIsEnumerable(final java.lang.String name) {
		setPropertyIsEnumerable(name, true);
	}

	public void setPropertyIsEnumerable(final java.lang.String name, final boolean isEnum) {}

	@Override
	public java.lang.String toString() {
		return "[object "+getClass().getName()+"]";
	}

	public Object valueOf() {
		return this;
	}

	public java.lang.Object JITB$getIndex(final int index) { throw new DynamicCodeException(); }

	public void JITB$setIndex(final int index, final java.lang.Object value) { throw new DynamicCodeException(); }

	public java.lang.Object JITB$getProperty(final java.lang.String property) { throw new DynamicCodeException(); }

	public void JITB$setProperty(final java.lang.String property, final java.lang.Object value) { throw new DynamicCodeException(); }
}
