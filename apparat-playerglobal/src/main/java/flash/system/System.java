package flash.system;

import jitb.errors.MissingImplementationException;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
public class System extends jitb.lang.Object {
	private static boolean _useCodePage = false;

	public System() {}

	public static IME ime() {
		return null;
	}

	public static boolean useCodePage() { return _useCodePage; }
	public static void useCodePage(final boolean value) { _useCodePage = value; }

	public static long totalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	@Metadata({@Element(name="Inspectable", keys={"environment"}, values={"none"})})
	public static String vmVersion() { return "jitb pre-alpha"; }

	public static void resume() {
		throw new MissingImplementationException("resume");
	}

	public static void setClipboard(final String string) {
		throw new MissingImplementationException("setClipboard");
	}

	public static void pause() {
		throw new MissingImplementationException("pause");
	}

	public static void gc() {
		java.lang.System.gc();
	}

	public static void exit(final long code) {
		java.lang.System.exit((int)code);	
	}
}
