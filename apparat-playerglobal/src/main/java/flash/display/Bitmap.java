package flash.display;

/**
 * @author Joa Ebert
 */
public class Bitmap extends DisplayObject {
	private BitmapData _bitmapData;
	private String _pixelSnapping;
	private boolean _smoothing;

	public Bitmap(final BitmapData bitmapData) {
		this(bitmapData, "auto");
	}

	public Bitmap(final BitmapData bitmapData, final String pixelSnapping) {
		this(bitmapData, pixelSnapping, false);
	}

	public Bitmap(final BitmapData bitmapData, final String pixelSnapping, final boolean smoothing) {
		_bitmapData = bitmapData;
		_pixelSnapping = pixelSnapping;
		_smoothing = smoothing;
	}

	public BitmapData bitmapData() { return _bitmapData; }
	public void bitmapData(final BitmapData value) { _bitmapData = value; }

	public String pixelSnapping() { return _pixelSnapping; }
	public void pixelSnapping(final String value) { _pixelSnapping = value; }

	public boolean smoothing() { return _smoothing; }
	public void smoothing(final Boolean value) { _smoothing = value; }
}
