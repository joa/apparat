package jitb.media;

import flash.media.Sound;
import flash.media.SoundTransform;
import javazoom.jl.decoder.*;

import java.io.InputStream;

/**
 * @author Joa Ebert
 */
public final class JLayerSoundSource implements ISoundSource {
	private final Bitstream _bitstream;
	private final Decoder _decoder;

	private SampleBuffer _output = null;
	private int _available;
	private int _readPos;

	private double _leftPeak = 0.0;
	private double _rightPeak = 0.0;
	private double _position = 0.0;

	private final Sound _sound;

	private SoundTransform _soundTransform = new SoundTransform();

	public JLayerSoundSource(final Sound sound, final InputStream input) {
		_sound = sound;
		_bitstream = new Bitstream(input);
		_decoder = new Decoder();
	}
	
	@Override
	public synchronized void setup() {
	}

	@Override
	public synchronized void tearDown() {
	}

	@Override
	public synchronized void close() {
		try {
			_bitstream.close();
		} catch(final BitstreamException e) {
			//
			// Ignore exception.
			//
		}
	}

	@Override
	public synchronized void render(final float[] buffer) {
		if(null == _output) {
			if(!decodeFrame()) {
				SoundSystem.detach(this);
				return;
			}
		}

		final int n = buffer.length;
		final short[] b = _output.getBuffer();

		float leftPeak = 0.0f;
		float rightPeak = 0.0f;

		int i = 0;

		while(i < n && _available > 0) {
			final float leftAmp = b[_readPos++] / 32768.0f;
			final float rightAmp = b[_readPos++] / 32768.0f;

			leftPeak = Math.max(leftPeak, Math.abs(leftAmp));
			rightPeak = Math.max(rightPeak, Math.abs(rightAmp));

			buffer[i++] = leftAmp;
			buffer[i++] = rightAmp;

			_available--;

			if(0 == _available) {
				_bitstream.closeFrame();

				if(!decodeFrame()) {
					SoundSystem.detach(this);
					return;
				}
			}
		}

		_leftPeak = leftPeak;
		_rightPeak = rightPeak;
		
		_position += n / 44100.0;
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

	private boolean decodeFrame() {
		try {
			final Header header = _bitstream.readFrame();

			if(null == header) {
				return false;
			}

			_output = (SampleBuffer) _decoder.decodeFrame(header, _bitstream);
			_available += _output.getBufferLength() >> 1;
			_readPos = 0;

			return true;
		} catch(final Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
