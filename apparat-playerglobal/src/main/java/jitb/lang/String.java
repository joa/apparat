package jitb.lang;

/**
 * @author Joa Ebert
 */
public final class String extends jitb.lang.Object {
	public static String valueOf(final java.lang.String value) {
		return new String(value);
	}
	
	private final java.lang.String _value;

	public String(final java.lang.String value) {
		_value = value;
	}

	public String(final String val) {
		this(val.toString());
	}

	public int length() {
		return _value.length();
	}
	
	public java.lang.String charAt() {
		return charAt(0);
	}

	public java.lang.String charAt(final double index) {
		return Character.toString(_value.charAt((int)index));
	}

	public double charCodeAt() {
		return charCodeAt(0);
	}

	public double charCodeAt(final double index) {
		return (double)(_value.charAt((int)index));
	}

	//etc. etc...

	@Override
	public java.lang.String toString() {
		return _value;
	}
}
