package jitb.lang;

import jitb.errors.ErrorUtil;

/**
 * @author Joa Ebert
 */
public class TypeError extends Error {
	public TypeError(final String message) {
		super(message);
		name = "TypeError";
	}

	@Override
	public int errorID() {
		final int result;

		if(message.equals(ErrorUtil.E_1009)) {
			result = 1009;
		} else {
			result = super.errorID();
		}

		return result;
	}
}
