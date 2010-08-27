package jitb;

/**
 * @author Joa Ebert
 */
public abstract class Function1<A, B> extends Function<B> {
	public abstract B apply1(final A value);
	//public abstract void applyVoid1(final A value);

	@Override
	@SuppressWarnings("unchecked")
	public B apply(final Object... parameters) { return apply1((A)parameters[0]); }

	/*@Override
	@SuppressWarnings("unchecked")
	public void applyVoid(final Object... parameters) { applyVoid1((A)parameters[0]); }*/
}
