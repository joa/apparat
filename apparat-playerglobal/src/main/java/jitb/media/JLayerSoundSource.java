package jitb.media;

import flash.media.SoundTransform;
import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;

import java.io.InputStream;

/**
 * @author Joa Ebert
 */
public class JLayerSoundSource implements ISoundSource {
	final private Bitstream bitstream;
	final private Decoder decoder;
	private AudioDevice audio;

	private SampleBuffer _output = null;
	private int _available;
	private int _readPos;

	public JLayerSoundSource(final InputStream input) {
		bitstream = new Bitstream(input);
		decoder = new Decoder();
	}
	
	@Override
	public void setup() {
	}

	@Override
	public void tearDown() {
	}

	@Override
	public void close() {
		try {
			bitstream.close();
		} catch(final BitstreamException e) {
			//
			// Ignore exception.
			//
		}
	}

	@Override
	public void render(final float[] buffer) {
		if(null == _output) {
			if(!decodeFrame()) {
				SoundSystem.detach(this);
				return;
			}
		}

		final int n = buffer.length;
		final short[] b = _output.getBuffer();

		int i = 0;

		while(i < n && _available > 0) {
			buffer[i++] = b[_readPos++] / 32768.0f;
			buffer[i++] = b[_readPos++] / 32768.0f;

			_available--;

			if(0 == _available) {
				bitstream.closeFrame();

				if(!decodeFrame()) {
					SoundSystem.detach(this);
					return;
				}
			}
		}
	}

	@Override
	public double leftPeak() {
		return 0;
	}

	@Override
	public double rightPeak() {
		return 0;
	}

	@Override
	public double position() {
		return 0;
	}

	@Override
	public SoundTransform soundTransform() {
		return null;
	}

	@Override
	public void soundTransform(final SoundTransform value) {
	}

	private boolean decodeFrame() {
		try {
			final Header header = bitstream.readFrame();

			if(null == header) {
				return false;
			}

			_output = (SampleBuffer)decoder.decodeFrame(header, bitstream);
			_available += _output.getBufferLength() >> 1;
			_readPos = 0;
			return true;
		} catch(final Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
