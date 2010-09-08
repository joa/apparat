package jitb.display;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.events.Event;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Joa Ebert
 */
public final class DisplaySystem {
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

	public static void enterFrame() {
		dispatchEvent(new Event(Event.ENTER_FRAME));
	}

	public static void render(final Stage stage) {
		stage.JITB$renderDisplayObject();
	}

	public static void exitFrame() {
		dispatchEvent(new Event(Event.EXIT_FRAME));
	}

	public static void dispatchEvent(final Event event) {
		synchronized(lock) {
			for(final DisplayObject displayObject : _displayObjects) {
				displayObject.dispatchEvent(event.clone());
			}
		}
	}
}
