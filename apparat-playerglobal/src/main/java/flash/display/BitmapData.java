package flash.display;

import flash.geom.Rectangle;

import java.nio.ByteBuffer;

/**
 * @author Joa Ebert
 */
public class BitmapData implements IBitmapDrawable {
	private int _width;
	private int _height;
	private boolean _transparent;
	private final Rectangle _rect;

	private ByteBuffer _buffer;
	private int _bytesPerPixel;
	private int _stripeSize;
	
	public BitmapData(final int width, final int height) {
		this(width, height, true);
	}

	public BitmapData(final int width, final int height, final boolean transparent) {
		this(width, height, transparent, 0);
	}

	public BitmapData(final int width, final int height, final boolean transparent, final long fillColor) {
		_width = width;
		_height = height;
		_rect = new Rectangle(0.0, 0.0, _width, _height);
		_transparent = transparent;
		_bytesPerPixel = transparent ? 4 : 3;
		_stripeSize = width * _bytesPerPixel;
		_buffer = ByteBuffer.allocate(width * height * _bytesPerPixel);
		fillRect(rect(), fillColor);
	}

	public int width() { return _width; }
	public int height() { return _height; }
	public Rectangle rect() { return _rect;	}

	public void fillRect(Rectangle rect, long color) {

	}

	public long getPixel(final int x, final int y) {
		return _buffer.get(y * _stripeSize + x * _bytesPerPixel);
	}
}
