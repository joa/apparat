package flash.display;

/**
 * @author Joa Ebert
 */
public class Stage extends DisplayObjectContainer {
	private double _rate = 32.0;
	private String _quality = "high";
	private String _scaleMode = "none";

	public void frameRate(double value) {
		_rate = value;
	}

	public double frameRate() {
		return _rate;
	}

	public void quality(String value) {
		_quality = value;
	}

	public String quality() {
		return _quality;
	}

	public void scaleMode(String value) {
		_scaleMode = value;
	}

	public String scaleMode() {
		return _scaleMode;
	}
}
