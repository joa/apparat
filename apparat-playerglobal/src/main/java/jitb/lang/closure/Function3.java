package jitb.lang.closure;

/**
 * @author Joa Ebert
 */
public abstract class Function3<A, B, C, R> extends Function<R> {
	public abstract R apply3(final jitb.lang.Object thisArg, final A value0, final B value1, final C value3);
	public abstract void applyVoid3(final jitb.lang.Object thisArg, final A value0, final B value1, final C value3);

	@Override
	@SuppressWarnings("unchecked")
	public R apply(final jitb.lang.Object thisArg, final Object... argArray) {
		return apply3(thisArg, (A)argArray[0], (B)argArray[1], (C)argArray[2]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void applyVoid(final jitb.lang.Object thisArg, final Object... argArray) {
		applyVoid3(thisArg, (A)argArray[0], (B)argArray[1], (C)argArray[2]);
	}
}
