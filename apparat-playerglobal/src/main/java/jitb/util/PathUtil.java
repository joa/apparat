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
