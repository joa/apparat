/**
 * @author Joa Ebert
 */
public class Math {
	public final static double E = Double.NaN;
	public final static double LN10 = Double.NaN;
	public final static double LN2 = Double.NaN;
	public final static double LOG10E = 0.4342944819032518;
	public final static double LOG2E = 1.4426950408889634;
	public final static double PI = 3.141592653589793;
	public final static double SQRT1_2 = 0.7071067811865476;
	public final static double SQRT2 = 1.4142135623730951;
	
	public static double abs(final double x) { return java.lang.Math.abs(x); }
	public static double random() { return java.lang.Math.random(); }
	public static double acos(final double x) { return java.lang.Math.acos(x); }
	public static double cos(final double x) { return java.lang.Math.cos(x); }
	public static double ceil(final double x) { return java.lang.Math.ceil(x); }
	public static double round(final double x) { return java.lang.Math.round(x); }
	public static double asin(final double x) { return java.lang.Math.asin(x); }
	public static double sin(final double x) { return java.lang.Math.sin(x); }
	public static double atan2(final double y,final double x) { return java.lang.Math.atan2(y, x); }
	public static double floor(final double x) { return java.lang.Math.floor(x); }
	public static double log(final double x) { return java.lang.Math.log(x); }
	public static double exp(final double x) { return java.lang.Math.exp(x); }
	public static double pow(final double x, final double y) { return java.lang.Math.pow(x, y); }
	public static double atan(final double x) { return java.lang.Math.atan(x); }
	public static double tan(final double x) { return java.lang.Math.tan(x); }
	public static double sqrt(double x) { return java.lang.Math.sqrt(x); }

	public static double min(final double x, final double y) { return x < y ? x : y; }
	public static double min(final double x, final double y, final double z, final double... rest) {
		double result = x;

		if(y < result) result = y;
		if(z < result) result = z;

		for(final double w : rest) {
			if(w < result) result = w;
		}

		return result;
	}

	public static double max(final double x, final double y) { return x > y ? x : y; }
	public static double max(final double x, final double y, final double z, final Double... rest) {
		double result = x;

		if(y > result) result = y;
		if(z > result) result = z;

		for(final double w : rest) {
			if(w > result) result = w;
		}

		return result;
	}
}
