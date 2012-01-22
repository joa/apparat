/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
