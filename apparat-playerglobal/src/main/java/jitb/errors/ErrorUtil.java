package jitb.errors;

import jitb.lang.TypeError;

/**
 * @author Joa Ebert
 */
public final class ErrorUtil {
	public final static String E_1009 = "Cannot access a property or method of a null object reference.";
	public final static String E_2007 = "Parameter %s must be non-null.";

	public static void flashThrow(final Object value) {
		throw new Throw(value);
	}

	public static TypeError error1009() {
		final TypeError result = new TypeError(E_1009);
		result.JITB$errorID(1009);
		return result;
	}

	public static TypeError error2007(final String parameter) {
		final TypeError result = new TypeError(String.format(E_2007, parameter));
		result.JITB$errorID(2007);
		return result;
	}

	private ErrorUtil() {}
}
