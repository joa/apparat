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
package flash.media;

import jitb.media.ISoundSource;
import jitb.media.SoundSystem;

/**
 * @author Joa Ebert
 */
public class SoundChannel {
	private final ISoundSource _source;

	SoundChannel(final ISoundSource source) {
		_source = source;

		SoundSystem.attach(_source);
	}

	public void stop() {
		SoundSystem.detach(_source);
	}

	public double leftPeak() {
		return _source.leftPeak();
	}

	public double rightPeak() {
		return _source.rightPeak();
	}

	public double position() {
		return _source.position();
	}

	public SoundTransform soundTransform() {
		 return _source.soundTransform();
	}

	public void soundTransform(final SoundTransform value) {
		_source.soundTransform(value);
	}
}
