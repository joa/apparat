package jitb;

/**
 * @author Joa Ebert
 */
public final class Throw extends RuntimeException {
	public final Object value;

	public Throw(final Object value) {
		this.value = value;
	}
}
