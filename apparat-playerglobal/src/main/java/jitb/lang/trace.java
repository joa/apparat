package jitb.lang;

/**
 * @author Joa Ebert
 */
public class trace extends jitb.lang.Object {
	public static java.lang.Object callStatic(final java.lang.Object... arguments) {
		final StringBuilder builder = new StringBuilder();
		final int n = arguments.length;
		final int m = n - 1;

		for(int i = 0; i < n; ++i) {
			if(null == arguments[i]) {
				builder.append("null");
			} else {
				builder.append(arguments[i].toString());
			}
			
			if(i != m) {
				builder.append(" ");
			}
		}

		System.out.println(builder.toString());
		return null;
	}
}
