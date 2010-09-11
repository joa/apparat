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
