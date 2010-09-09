package jitb.util;

import jitb.lang.AVM;

import java.io.File;

/**
 * @author Joa Ebert
 */
public final class PathUtil {
	public static String createPath(final String pathname) {
		if(pathname.indexOf('/') == 0 || pathname.indexOf(':') == 1) {
			return pathname;
		}

		final String basePath = AVM.basePath();
		char separatorChar;

		if(basePath.startsWith("/") || basePath.startsWith("http://") || basePath.startsWith("ftp://")) {
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
