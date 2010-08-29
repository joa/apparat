package jitb.errors;

import jitb.errors.ErrorUtil;

/**
 * @author Joa Ebert
 */
public final class Require {
	public static void nonNull(final String parameter, final Object value) {
		if(null == value) {
			ErrorUtil.flashThrow(ErrorUtil.error2007(parameter));
		}
	}
	private Require() {}
}
