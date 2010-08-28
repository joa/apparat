package jitb.errors;

import jitb.errors.ErrorUtil;

/**
 * @author Joa Ebert
 */
public final class Require {
	public static void nonNull(final Object value) {
		if(null == value) {
			ErrorUtil.flashThrow(ErrorUtil.error1009());
		}
	}
	private Require() {}
}
