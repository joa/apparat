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
package jitb.util;

import jitb.lang.AVM;

import java.io.File;

/**
 * @author Joa Ebert
 */
public final class PathUtil {
	public static String createPath(final String pathname) {
		final String basePath = AVM.basePath();
		final char separatorChar;

		if(null == basePath || pathname.indexOf('/') == 0 || pathname.indexOf(':') == 1) {
			return pathname;
		}

		if(basePath.startsWith("/") || basePath.startsWith("http://") ||
			basePath.startsWith("https://") || basePath.startsWith("ftp://")) {
			separatorChar = '/';
		} else if(basePath.indexOf(':') == 1) {
			separatorChar = '\\';
		} else {
			separatorChar = File.separatorChar;
		}

		return AVM.basePath()+separatorChar+pathname;
	}

	private PathUtil() {}
}
