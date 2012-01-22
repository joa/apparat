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
package flash.system;

import jitb.errors.MissingImplementationException;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
public class System extends jitb.lang.Object {
	private static boolean _useCodePage = false;

	public System() {}

	public static IME ime() {
		return null;
	}

	public static boolean useCodePage() { return _useCodePage; }
	public static void useCodePage(final boolean value) { _useCodePage = value; }

	public static long totalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	@Metadata({@Element(name="Inspectable", keys={"environment"}, values={"none"})})
	public static String vmVersion() { return "jitb pre-alpha"; }

	public static void resume() {
		throw new MissingImplementationException("resume");
	}

	public static void setClipboard(final String string) {
		throw new MissingImplementationException("setClipboard");
	}

	public static void pause() {
		throw new MissingImplementationException("pause");
	}

	public static void gc() {
		java.lang.System.gc();
	}

	public static void exit(final long code) {
		java.lang.System.exit((int)code);
	}
}
