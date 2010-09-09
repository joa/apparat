package flash.media;

/**
 * @author Joa Ebert
 */
public class SoundLoaderContext {
	public double bufferTime;
	public boolean checkPolicyFile;

	public SoundLoaderContext() {
		this(1000.0);
	}

	public SoundLoaderContext(final double bufferTime) {
		this(bufferTime, false);
	}

	public SoundLoaderContext(final double bufferTime, final boolean checkPolicyFile) {
		this.bufferTime = bufferTime;
		this.checkPolicyFile = checkPolicyFile;
	}
}
