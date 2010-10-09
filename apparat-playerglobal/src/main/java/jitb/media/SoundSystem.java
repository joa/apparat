package jitb.media;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The SoundSystem class is a manager for audio streams in
 * the JITB media system.
 * 
 * @author Joa Ebert
 */
public final class SoundSystem {
	/**
	 * The sample rate in Hz.
	 *
	 * <p>Flash Player uses a default sample rate of 44.1kHz.</p>
	 */
	public static final float SAMPLE_RATE = 44100.0f;

	/**
	 * The number of samples per block.
	 */
	public static final int NUM_SAMPLES = 0x2000;

	/**
	 * The size of the internal float buffer.
	 *
	 * <p>This value is calculated via <code>samples * channels</code>.</p>
	 */
	private static final int FLOAT_BUFFER = NUM_SAMPLES << 1;

	/**
	 * The size of the internal byte buffer which is sent to the
	 * sound card.
	 *
	 * <p>This value is calculated via <code>samples * channels * bytesPerChannel</code>.</p>
	 */
	private static final int BYTE_BUFFER = NUM_SAMPLES << 2;

	/**
	 * The lock object for the <code>_sources</code> list.
	 */
	private static final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

	/**
	 * The <code>_sources</code> read lock.
	 */
	private static final Lock _readLock = _lock.readLock();

	/**
	 * The <code>_sources</code> write lock.
	 */
	private static final Lock _writeLock = _lock.writeLock();

	/**
	 * The list of sound sources.
	 *
	 * <p>This list is usually written from ActionScript and queried from
	 * the mixer thread.</p>
	 */
	private static final List<ISoundSource> _sources = new LinkedList<ISoundSource>();

	/**
	 * The float buffer which is passed to the sources.
	 *
	 * Each source should add to the value of the buffer.
	 * The buffer is cleared before all buffers contribute their amplitude.
	 */
	private static final float[] _floatBuffer = new float[FLOAT_BUFFER];

	/**
	 * A buffer which is passed to the sound card.
	 */
	private static final byte[] _buffer = new byte[BYTE_BUFFER];

	/**
	 * The internal audio format which is lazy initialized.
	 * @see #audioFormat
	 */
	private static AudioFormat _audioFormat = null;

	/**
	 * The lock object for the <code>_sourceDataLine</code>.
	 */
	private static final ReentrantLock _sourceDataLineLock = new ReentrantLock();

	/**
	 * The Java SourceDataLine which is lazy initialized.
	 *
	 * @see #start
	 * @see #stop
	 */
	private static SourceDataLine _sourceDataLine = null;

	/**
	 * The AudioMixer class is lazy created and runs via
	 * the <code>_mixer</code> thread.
	 */
	private static final class AudioMixer implements Runnable {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				while(!Thread.interrupted()) {
					//
					// Clear the amplitude buffer.
					//
					Arrays.fill(_floatBuffer, 0.0f);

					_readLock.lockInterruptibly();

					try {
						//
						// Let all sources render their amplitudes into
						// amplitude buffer.
						//
						for(final ISoundSource source : _sources) {
							source.render(_floatBuffer);
						}
					} finally {
						_readLock.unlock();
					}

					//
					// Fill the byte buffer with the values from the
					// amplitude buffer.
					//

					for(int i = 0; i < FLOAT_BUFFER; ++i) {
						final float amplitudeFloat = _floatBuffer[i];
						final float finalAmplitude;
						final int amplitudeShort;

						if(amplitudeFloat < -1.0f) {
							finalAmplitude = -1.0f;
						} else if(amplitudeFloat > 1.0f) {
							finalAmplitude = 1.0f;
						} else {
							finalAmplitude = amplitudeFloat;
						}

						amplitudeShort = Math.round(finalAmplitude * 0.96f * 0x8000);

						_buffer[(i << 1)] = (byte)(amplitudeShort & 0xff);
						_buffer[(i << 1) + 1] = (byte)((amplitudeShort >>> 0x08) & 0xff);
					}

					//
					// Send the byte buffer to the sound card.
					//

					try {
						_sourceDataLineLock.lockInterruptibly();
						if(null != _sourceDataLine) {
							_sourceDataLine.write(_buffer, 0, _buffer.length);
						}
					} finally {
						_sourceDataLineLock.unlock();
					}

					//
					// Sleep for an amount of time that is appropriate for
					// the NUM_SAMPLES value.
					//

					Thread.sleep(100);
				}
			} catch(final InterruptedException iex) {
				//
				// The audio system has been stopped.
				//
			}
		}
	}

	/**
	 * The lazy initialized audio mixer.
	 */
	private static AudioMixer _mixer = null;

	/**
	 * The mixer thread.
	 */
	private static Thread _mixerThread = null;

	/**
	 * Creates and returns an AudioFormat object.
	 *
	 * @return The AudioFormat object.
	 */
	private static AudioFormat audioFormat() {
		if(null == _audioFormat) {
			_audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
																		 SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false);
		}

		return _audioFormat;
	}

	/**
	 * Starts the audio playback.
	 */
	private static void start() {
		try {
			_sourceDataLineLock.lock();

			//
			// Check if we are already running.
 			//

			if(null != _sourceDataLine) {
				return;
			}

			final AudioFormat audioFormat = audioFormat();
			final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat());

			//
			// Create a new SourceDataLine.
			//

			SourceDataLine line = null;

			try {
				line = (SourceDataLine)AudioSystem.getLine(info);
				line.open(audioFormat);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(null != line) {
				//
				// We have a SourceDataLine so let's start the mixer.
				//

				_sourceDataLine = line;
				_sourceDataLine.start();
				_mixer = new AudioMixer();
				_mixerThread = new Thread(_mixer, "SoundSystem");
				_mixerThread.setPriority(Thread.MAX_PRIORITY);
				_mixerThread.start();
			}
		} finally {
			_sourceDataLineLock.unlock();
		}
	}

	private static void stop() {
		//
		// Interrupt the mixer if running.
		//

		if(null != _mixerThread) {
			_mixerThread.interrupt();
			_mixerThread = null;
			_mixer = null;
		}

		try {
			_sourceDataLineLock.lock();

			if(null == _sourceDataLine) {
				return;
			}

			//
			// Close the SourceDataLine.
			//

			_sourceDataLine.stop();
			_sourceDataLine.close();
			_sourceDataLine = null;
		} finally {
			_sourceDataLineLock.unlock();
		}
	}

	/**
	 * Attaches an ISoundSource to the sound system.
	 *
	 * @param source The sound source to attach.
	 */
	public static void attach(final ISoundSource source) {
		_writeLock.lock();

		try {
			_sources.add(source);
		} finally {
			_writeLock.unlock();
		}

		source.setup();
		start();
	}

	/**
	 * Detaches an ISoundSource from the sound system.
	 *
	 * @param source The sound source to detach.
	 */
	public static void detach(final ISoundSource source) {
		_writeLock.lock();

		try {
			_sources.remove(source);
		} finally {
			_writeLock.unlock();
		}

		source.tearDown();
		stop();
	}

	/**
	 * Stops audio playback and releases resources used from the
	 * SoundSystem.
	 */
	public static void shutdown() {
		_readLock.lock();

		try {
			//
			// Notify all sources about the shutdown.
			//

			for(final ISoundSource source : _sources) {
				source.tearDown();
				source.close();
			}
		} finally {
			_readLock.unlock();
		}

		_writeLock.lock();

		try {
			_sources.clear();
		} finally {
			_writeLock.unlock();
		}

		stop();
	}

	public static float[] newBuffer() {
		return newBuffer(1);
	}

	/**
	 * Creates and returns a new buffer of floats according
	 * to the current buffer size.
	 *
	 * @param multiplier The multiplier of the buffer length;
	 * 
	 * @return An empty array of floats.
	 */
	public static float[] newBuffer(final int multiplier) {
		return new float[FLOAT_BUFFER * multiplier];
	}
}
