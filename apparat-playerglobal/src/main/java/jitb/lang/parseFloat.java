package jitb.lang;

/**
 * @author Joa Ebert
 */
public class parseFloat extends jitb.lang.Object {
	public static double callStatic() {
		return callStatic("NaN");
	}
	
	public static double callStatic(final java.lang.String str) {
		return Double.parseDouble(str);
	}
}
