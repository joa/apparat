package jitb;

/**
 * @author Joa Ebert
 */
public final class Require {
	public static void nonNull(final Object value) {
		if(null == value) {
			Errors.flashThrow(Errors.error1009());
		}
	}
	private Require() {}
}
