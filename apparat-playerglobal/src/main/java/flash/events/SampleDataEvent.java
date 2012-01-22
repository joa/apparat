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

import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class SampleDataEvent extends Event {
	public static final String SAMPLE_DATA = "sampleData";

	private double _position;
	private ByteArray _data;

	public SampleDataEvent(final String type) {
		this(type, false);
	}

	public SampleDataEvent(final String type, final boolean bubbles) {
		this(type, bubbles, false);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable) {
		this(type, bubbles, cancelable, 0.0);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable, final double theposition) {
		this(type, bubbles, cancelable, theposition, null);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable, final double theposition, final ByteArray thedata) {
		super(type, bubbles, cancelable);

		_position = theposition;
		_data = thedata;
	}

	public double position() { return _position; }
	public void position(final double value) { _position = value; }

	public ByteArray data() { return _data; }
	public void data(final ByteArray value) { _data = value; }

	@Override
	public String toString() {
		return "[SampleDataEvent]";
	}

	@Override
	public Event clone() {
		return new SampleDataEvent(type(), bubbles(), cancelable(), position(), data());
	}
}
