package jitb.lang;

import flash.utils.getTimer;
import jitb.errors.ErrorUtil;
import jitb.errors.Require;
import jitb.events.EventSystem;
import jitb.media.SoundSystem;

/**
 * @author Joa Ebert
 */
public final class AVM {
	private static String _basePath = "";

	public static void basePath(final String value) { _basePath = value; }
	public static String basePath() { return _basePath; }
	
	public static void start() {
		getTimer.JITB$init = System.currentTimeMillis();
	}

	public static void stop() {
		SoundSystem.shutdown();
		EventSystem.shutdown();
	}
	
	@SuppressWarnings("unchecked")
	public static void coerce(final java.lang.Object value, final java.lang.Class type) {
		if(!type.isAssignableFrom(value.getClass())) {
			ErrorUtil.flashThrow(ErrorUtil.error1034(value, type));
		}
	}

	public static java.lang.Object getProperty(final java.lang.Object object, final java.lang.Object property) {
		Require.nonNull("object", object);
		Require.nonNull("property", property);

		try {
			return object.getClass().getField(property.toString()).get(object);
		} catch(NoSuchFieldException e) {
			return null;//undefined
		} catch(IllegalAccessException e) {
			return null;//undefined
		}
	}

	public static void setProperty(final java.lang.Object object, final java.lang.Object property, final java.lang.Object value) {
		Require.nonNull("object", object);
		Require.nonNull("property", property);

		try {
			object.getClass().getField(property.toString()).set(object, value);
		} catch(NoSuchFieldException e) {
			// Nothing to do here.
		} catch(IllegalAccessException e) {
			// Nothing to do here.
		}
	}

	private AVM() {}
}
