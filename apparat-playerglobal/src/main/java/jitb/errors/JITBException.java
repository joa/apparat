package jitb.errors;

/**
 * @author Joa Ebert
 */
public class JITBException extends RuntimeException {
	public JITBException(final String message) {
		super(message);
	}

	public JITBException(final Throwable cause) {
		super(cause);
	}

	public JITBException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JITBException() {
		super();
	}
}
