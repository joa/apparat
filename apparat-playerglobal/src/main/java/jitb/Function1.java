package jitb;

/**
 * @author Joa Ebert
 */
public abstract class Function1<A, B> extends Function<B> {
	public abstract B apply1(A value);

	@Override
	@SuppressWarnings("unchecked")
	public B apply(Object... parameters) {
		return apply1((A)parameters[0]);
	}
}
