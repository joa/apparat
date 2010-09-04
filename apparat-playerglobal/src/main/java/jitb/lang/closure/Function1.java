package jitb.lang.closure;

/**
 * @author Joa Ebert
 */
public abstract class Function1<A, R> extends Function<R> {
	public abstract R apply1(final jitb.lang.Object thisArg, final A value);
	public abstract void applyVoid1(final jitb.lang.Object thisArg, final A value);

	@Override
	@SuppressWarnings("unchecked")
	public R apply(final jitb.lang.Object thisArg, final java.lang.Object... argArray) {
		return apply1(thisArg, (A)argArray[0]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void applyVoid(final jitb.lang.Object thisArg, final java.lang.Object... argArray) {
		applyVoid1(thisArg, (A)argArray[0]);
	}
}
