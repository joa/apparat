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
package jitb.errors;

import jitb.lang.TypeError;

/**
 * @author Joa Ebert
 */
public final class ErrorUtil {
	public final static String E_1009 = "Cannot access a property or method of a null object reference.";
	public final static String E_1034 = "Type Coercion failed: cannot convert %s to %s.";
	public final static String E_2007 = "Parameter %s must be non-null.";

	public static void flashThrow(final Object value) {
		throw new Throw(value);
	}

	public static TypeError error1009() {
		final TypeError result = new TypeError(E_1009);
		result.JITB$errorID(1009);
		return result;
	}

	public static TypeError error1034(final Object object, final Class type) {
		final TypeError result = new TypeError(String.format(E_1034, object, type.getName()));
		result.JITB$errorID(1034);
		return result;
	}

	public static TypeError error2007(final String parameter) {
		final TypeError result = new TypeError(String.format(E_2007, parameter));
		result.JITB$errorID(2007);
		return result;
	}

	private ErrorUtil() {}
}
