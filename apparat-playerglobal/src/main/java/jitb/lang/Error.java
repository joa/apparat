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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Joa Ebert
 */
public class Error extends jitb.lang.Object {
	private int _errorID;
	private final Throwable _throwable = new Throwable();

	public java.lang.String message;
	public java.lang.String name = "Error";

	public Error() {
		this("");
	}

	public Error(final java.lang.String message) {
		this(message, 0);
	}

	public Error(final java.lang.String message, final int id) {
		this.message = message;
		_errorID = id;
	}

	public int errorID() {
		return _errorID;
	}

	public void JITB$errorID(final int value) {
		_errorID = value;
	}

	public java.lang.String getStackTrace() {
		final StringWriter stringWriter = new StringWriter();

		_throwable.printStackTrace(new PrintWriter(stringWriter));

		stringWriter.flush();

		return stringWriter.toString();
	}
}
