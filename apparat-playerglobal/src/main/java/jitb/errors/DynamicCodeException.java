package jitb.errors;

/**
 * @author Joa Ebert
 */
public class DynamicCodeException extends JITBException {
	public DynamicCodeException() {
		super("Cannot execute dynamic code.");
	}
}
