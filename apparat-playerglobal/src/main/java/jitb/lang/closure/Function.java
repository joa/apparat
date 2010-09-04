package jitb.lang.closure;

/**
 * The Function class defines an abstract function closure.
 *
 * @author Joa Ebert
 */
public abstract class Function<R> extends jitb.lang.Object {
	public abstract R apply(final jitb.lang.Object thisArg, final java.lang.Object ...argArray);
	public abstract void applyVoid(final jitb.lang.Object thisArg, final java.lang.Object ...argArray);

	public R call(final jitb.lang.Object thisArg, final java.lang.Object ...argArray) {
		return apply(thisArg, argArray);
	}

	public void callVoid(final jitb.lang.Object thisArg, final java.lang.Object ...argArray) {
		applyVoid(thisArg, argArray);
	}
}
