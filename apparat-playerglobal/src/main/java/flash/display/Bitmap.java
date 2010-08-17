package flash.display;

import org.lwjgl.opengl.GL11;

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

	public double width() {
		return null == _bitmapData ? 0.0 : _bitmapData.width();
	}

	public double height() {
		return null == _bitmapData ? 0.0 : _bitmapData.height();
	}

	@Override
	public final void render() {
		if(null == _bitmapData) {
			return;
		}

		final int width = (int)(width() + 0.5);
		final int height = (int)(height() + 0.5);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, _bitmapData.textureId());
		GL11.glBegin(GL11.GL_QUADS); {
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex2i(0, 0);
			
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex2i(width, 0);

			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex2i(width, height);

			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex2i(0, height);
		} GL11.glEnd();
	}
}
