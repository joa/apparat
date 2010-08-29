package jitb.errors;

/**
 * @author Joa Ebert
 */
public final class DynamicCodeException extends JITBException {
	public DynamicCodeException() {
		super("Cannot execute dynamic code.");
	}
}
