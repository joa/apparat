package jitb.lang;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Joa Ebert
 */
public class Error extends jitb.lang.Object {
	private final int _errorID;
	private final Throwable _throwable = new Throwable();

	public String message;
	public String name = "Error";

	public Error() {
		this("");
	}

	public Error(final String message) {
		this(message, 0);
	}

	public Error(final String message, final int id) {
		this.message = message;
		_errorID = id;
	}
	
	public int errorID() {
		return _errorID;
	}

	public String getStackTrace() {
		final StringWriter stringWriter = new StringWriter();

		_throwable.printStackTrace(new PrintWriter(stringWriter));

		stringWriter.flush();
		
		return stringWriter.toString();
	}
}
