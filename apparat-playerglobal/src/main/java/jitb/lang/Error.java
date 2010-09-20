package jitb.lang;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Joa Ebert
 */
public class Error extends jitb.lang.Object {
	private int _errorID;
	private final Throwable _throwable = new Throwable();

	public java.lang.String message;
	public java.lang.String name = "Error";

	public Error() {
		this("");
	}

	public Error(final java.lang.String message) {
		this(message, 0);
	}

	public Error(final java.lang.String message, final int id) {
		this.message = message;
		_errorID = id;
	}
	
	public int errorID() {
		return _errorID;
	}

	public void JITB$errorID(final int value) {
		_errorID = value;
	}

	public java.lang.String getStackTrace() {
		final StringWriter stringWriter = new StringWriter();

		_throwable.printStackTrace(new PrintWriter(stringWriter));

		stringWriter.flush();
		
		return stringWriter.toString();
	}
}
