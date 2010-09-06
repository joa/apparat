package flash.utils;

/**
 * @author Joa Ebert
 */
public class getTimer extends jitb.lang.Object {
	public static long JITB$init = 0L;
	public static int callStatic() {
		return (int)(System.currentTimeMillis() - JITB$init);
	}
}
