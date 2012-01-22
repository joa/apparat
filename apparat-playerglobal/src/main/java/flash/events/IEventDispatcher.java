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
package flash.events;

import jitb.lang.closure.Function;
import jitb.lang.closure.Function1;

/**
 * @author Joa Ebert
 */
public interface IEventDispatcher {
	boolean dispatchEvent(final Event event);
	boolean hasEventListener(final String type);
	boolean willTrigger(final String type);
	void addEventListener(final String type, final Function1<Event, Object> listener);
	void addEventListener(final String type, final Function1<Event, Object> listener, final boolean useCapture);
	void addEventListener(final String type, final Function1<Event, Object> listener, final boolean useCapture,
						  final int priority);
	void addEventListener(final String type, final Function1<Event, Object> listener, final boolean useCapture,
						  final int priority, final boolean useWeakReference);

	void removeEventListener(final String type, final Function1<Event, Object> listener);
	void removeEventListener(final String type, final Function1<Event, Object> listener, final boolean useCapture);

	void addEventListener(final String type, final Function<Object> listener);
	void addEventListener(final String type, final Function<Object> listener, final boolean useCapture);
	void addEventListener(final String type, final Function<Object> listener, final boolean useCapture,
						  final int priority);
	void addEventListener(final String type, final Function<Object> listener, final boolean useCapture,
						  final int priority, final boolean useWeakReference);

	void removeEventListener(final String type, final Function<Object> listener);
	void removeEventListener(final String type, final Function<Object> listener, final boolean useCapture);
}
