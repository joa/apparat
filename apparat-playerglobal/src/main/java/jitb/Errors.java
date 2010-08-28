package jitb;

import jitb.lang.TypeError;

/**
 * @author Joa Ebert
 */
public final class Errors {
	public final static String E_1009 = "Cannot access a property or method of a null object reference";

	public static void flashThrow(final Object value) {
		throw new Throw(value);
	}

	public static TypeError error1009() {
		return new TypeError(E_1009);
	}

	private Errors() {}
}
