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
package jitb.media;

import flash.media.SoundTransform;

/**
 * @author Joa Ebert
 */
public interface ISoundSource {
	/**
	 * Called before this ISoundSource starts playback.
	 */
	void setup();

	/**
	 * Called when playback stops.
	 */
	void tearDown();

	/**
	 * Called when this ISoundSource is no longer required.
	 */
	void close();

	/**
	 * Renders the audio material into a given buffer of two
	 * channels.
	 *
	 * An ISoundSource has to add to this buffer since it might
	 * be already populated with other values.
	 *
	 * @param buffer The audio buffer to fill.
	 */
	void render(final float[] buffer);

	/**
	 * The peak of the left channel.
	 *
	 * Access to this method has to be synchronized.
	 *
	 * @return A normalized amplitude.
	 */
	double leftPeak();

	/**
	 * The peak of the right channel.
	 *
	 * Access to this method has to be synchronized.
	 *
	 * @return A normalized amplitude.
	 */
	double rightPeak();

	/**
	 * The number of milliseconds that elapes since playback.
	 *
	 * Access to this method has to be synchronized.
	 *
	 * @return Elapsed time in milliseconds.
	 */
	double position();

	/**
	 * The SoundTransform object associated with this ISoundSource.
	 *
	 * Access to this method has to be synchronized.
	 *
	 * @return A SoundTransform object.
	 */
	SoundTransform soundTransform();

	/**
	 * Sets the SoundTransform for this ISoundSource.
	 *
	 * Access to this method has to be synchronized.
	 *
	 * @param value The new value for the SoundTransform.
	 */
	void soundTransform(final SoundTransform value);
}
