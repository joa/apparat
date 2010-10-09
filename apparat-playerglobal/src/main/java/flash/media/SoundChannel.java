
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
