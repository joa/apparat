package jitb.display;

import flash.display.DisplayObject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Joa Ebert
 */
public final class DisplayList {
	private static final Object lock = new Object();
	private static final Set<DisplayObject> _displayObjects =
			new LinkedHashSet<DisplayObject>();

	public static void register(final DisplayObject displayObject) {
		synchronized(lock) {
			_displayObjects.add(displayObject);
		}
	}

	public static Set<DisplayObject> displayObjects() {
		synchronized(lock) {
			return Collections.unmodifiableSet(_displayObjects);
		}
	}
}
