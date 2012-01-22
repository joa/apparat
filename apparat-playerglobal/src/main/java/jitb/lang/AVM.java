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
	private static java.lang.String _basePath = "";

	public static void basePath(final java.lang.String value) { _basePath = value; }
	public static java.lang.String basePath() { return _basePath; }

	public static void start() {
		getTimer.JITB$init = System.currentTimeMillis();
	}

	public static void stop() {
		SoundSystem.shutdown();
		EventSystem.shutdown();
	}

	@SuppressWarnings("unchecked")
	public static void coerce(final java.lang.Object value, final java.lang.Class type) {
		if(null != value && !type.isAssignableFrom(value.getClass())) {
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

	public static java.lang.String escapeXMLElement(final java.lang.String value) {
		//TODO implement complete entity list
		final int n = value.length();
		final StringBuffer buffer = new StringBuffer(n << 1);

		for(int i = 0; i < n; ++i) {
			final char c = value.charAt(i);

			if(c == '"') {
				buffer.append("&quot;");
			} else if(c == '&') {
				buffer.append("&amp;");
			} else if(c == '<') {
				buffer.append("&lt;");
			} else if(c == '>') {
				buffer.append("&gt;");
			} else if(c > 0x7f) {
				buffer.append("&#");
				buffer.append((int)c);
				buffer.append(';');
			} else {
				buffer.append(c);
			}
		}

		return buffer.toString();
	}

	public static java.lang.String escapeXMLAttribute(final java.lang.String value) {
		//TODO implement me
		return escapeXMLElement(value);
	}

	private AVM() {}
}
