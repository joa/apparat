package jitb.errors;

/**
 * @author Joa Ebert
 */
public final class MissingImplementationException extends JITBException {
	public MissingImplementationException(final String method) {
		super("Missing implementation of "+method+".");
	}
}
