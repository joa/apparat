package jitb.lang.closure;

/**
 * The Function class defines an abstract function closure.
 *
 * @author Joa Ebert
 */
public abstract class Function<B> extends jitb.lang.Object {
	/**
	 * Applies the given parameters to the function.
	 *
	 * @param parameters The parameters of the function.
	 * @return The result of the function, <code>null</code> if undefined.
	 */
	public abstract B apply(final Object... parameters);

	/**
	 * Applies the given parameters to the function.
	 *
	 * @param parameters The parameters of the function.
	 */
	//public abstract void applyVoid(final Object... parameters);
}
