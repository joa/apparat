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

/**
 * @author Joa Ebert
 */
public final class String extends jitb.lang.Object {
	public static String valueOf(final java.lang.String value) {
		return new String(value);
	}

	private final java.lang.String _value;

	public String(final java.lang.String value) {
		_value = value;
	}

	public String(final String val) {
		this(val.toString());
	}

	public int length() {
		return _value.length();
	}

	public java.lang.String charAt() {
		return charAt(0);
	}

	public java.lang.String charAt(final double index) {
		return Character.toString(_value.charAt((int)index));
	}

	public double charCodeAt() {
		return charCodeAt(0);
	}

	public double charCodeAt(final double index) {
		return (double)(_value.charAt((int)index));
	}

	//etc. etc...

	@Override
	public java.lang.String toString() {
		return _value;
	}
}
