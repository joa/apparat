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

import flash.events.SampleDataEvent;
import flash.media.Sound;
import flash.media.SoundTransform;
import flash.utils.ByteArray;
import jitb.events.EventSystem;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Joa Ebert
 */
public final class AS3SoundSource implements ISoundSource, Runnable {
	private final Sound _sound;
	private double _leftPeak = 0.0;
	private double _rightPeak = 0.0;
	private SoundTransform _soundTransform = new SoundTransform();
	private double _position = 0.0;
	private final ByteArray _byteArray = new ByteArray();
	private final ReentrantLock _lock = new ReentrantLock();
	private final float[] _buffer = SoundSystem.newBuffer(2);
	private int _bufferWritePos = 0;
	private int _bufferReadPos = 0;
	private int _available = 0;
	private boolean _isClosed = false;

	public AS3SoundSource(final Sound sound) {
		_sound = sound;
	}

	@Override
	public synchronized void setup() {
		Arrays.fill(_buffer, 0.0f);
		_bufferReadPos = 0;
		_bufferWritePos = 0;
		requestAudio();
	}

	@Override
	public synchronized void tearDown() {
		_lock.lock();

		try {
			_isClosed = true;
		} finally {
			_lock.unlock();
		}
	}

	@Override
	public synchronized void close() {

	}

	@Override
	public synchronized void render(final float[] buffer) {
		_lock.lock();

		try {
			final int n = buffer.length;
			final int bufferLength = _buffer.length;

			float leftPeak = 0.0f;
			float rightPeak = 0.0f;

			int i = 0;

			while(i < n && _available > 0) {
				final float leftAmp = _buffer[_bufferReadPos++];
				final float rightAmp = _buffer[_bufferReadPos++];

				leftPeak = Math.max(leftPeak, Math.abs(leftAmp));
				rightPeak = Math.max(rightPeak, Math.abs(rightAmp));

				buffer[i++] += leftAmp;
				buffer[i++] += rightAmp;

				if(_bufferReadPos == bufferLength) {
					_bufferReadPos = 0;
				}

				_available--;
			}

			_leftPeak = leftPeak;
			_rightPeak = rightPeak;
			_position += n / 44100.0;

			if(!_isClosed && _available < SoundSystem.NUM_SAMPLES) {
				requestAudio();
			}
		} finally {
			_lock.unlock();
		}
	}

	@Override
	public synchronized double leftPeak() {
		return _leftPeak;
	}

	@Override
	public synchronized double rightPeak() {
		return _rightPeak;
	}

	@Override
	public synchronized double position() {
		return _position;
	}

	@Override
	public synchronized SoundTransform soundTransform() {
		return _soundTransform;
	}

	@Override
	public synchronized void soundTransform(final SoundTransform value) {
		_soundTransform = value;
	}

	@Override
	public void run() {
		_lock.lock();

		try {
			_byteArray.position(0);

			final int bufferLength = _buffer.length;
			final int n = (int)_byteArray.bytesAvailable() >> 3;

			_available += n;

			int i = 0;
			final ByteBuffer buffer =_byteArray.JITB$buffer();

			while(i < n) {
				final float leftAmp = buffer.getFloat();
				final float rightAmp = buffer.getFloat();

				_buffer[_bufferWritePos++] = leftAmp;
				_buffer[_bufferWritePos++] = rightAmp;

				if(_bufferWritePos == bufferLength) {
					_bufferWritePos = 0;
				}

				i++;
			}

			_byteArray.position(0);

			if(!_isClosed && _available < SoundSystem.NUM_SAMPLES) {
				requestAudio(true);
				run();
			}
		} finally {
			_lock.unlock();
		}
	}

	private void requestAudio() {
		requestAudio(false);
	}

	private void requestAudio(final boolean direct) {
		if(direct) {
			_sound.dispatchEvent(newSampleDataEvent());
		} else {
			EventSystem.callbackDispatch(_sound,
				newSampleDataEvent(), this);
		}
	}

	private SampleDataEvent newSampleDataEvent() {
		_byteArray.position(0);
		return new SampleDataEvent(SampleDataEvent.SAMPLE_DATA, false, false, _position, _byteArray);
	}
}
