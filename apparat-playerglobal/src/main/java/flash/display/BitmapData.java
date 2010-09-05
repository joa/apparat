package flash.display;

import flash.geom.Rectangle;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Joa Ebert
 */
public class BitmapData extends jitb.lang.Object implements IBitmapDrawable {
	private final static IntBuffer _textureIdBuffer = BufferUtils.createIntBuffer(1);

	private int _width;
	private int _height;
	private boolean _transparent;
	private final Rectangle _rect;

	private boolean _invalidated;
	private ByteBuffer _buffer;
	private int _textureId = -1;

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
		_buffer = BufferUtils.createByteBuffer(width * height * 4);
		fillRect(_rect, fillColor);
	}

	public int width() { return _width; }
	public int height() { return _height; }
	public Rectangle rect() { return _rect;	}

	public void fillRect(final Rectangle rect, final long color) {
		_invalidated = true;

		final int minX = (int)rect.x();
		final int minY = (int)rect.y();
		final int maxX = (int)(rect.x() + rect.width());
		final int maxY = (int)(rect.y() + rect.height());

		if(0 == minX && 0 == minY && _width == maxX && _height == maxY) {
			if(0 == color) {
				final int n = _width * _height * 4;
				final byte zero = (byte)0;
				_buffer.clear();
				for(int i = 0; i < n; ++i) {
					_buffer.put(zero);
				}
				_buffer.flip();
			} else {
				final byte alpha = (byte)((color & 0xff000000L) >>> 0x18);
				final byte red = (byte)((color & 0xff0000L) >>> 0x10);
				final byte green = (byte)((color & 0xff00L) >>> 0x08);
				final byte blue = (byte)(color & 0xffL);
				final int n = _width * _height;
				_buffer.clear();
				for(int i = 0; i < n; ++i) {
					_buffer.put(red).put(green).put(blue).put(alpha);
				}
				_buffer.flip();
			}
		} else {
			final byte alpha = (byte)((color & 0xff000000L) >>> 0x18);
			final byte red = (byte)((color & 0xff0000L) >>> 0x10);
			final byte green = (byte)((color & 0xff00L) >>> 0x08);
			final byte blue = (byte)(color & 0xffL);

			int oy = 0;
			for(int y = minY; y < maxY; ++y) {
				oy = y * _width * 4;
				for(int x = minX; x < maxX; ++x) {
					final int index = oy + x * 4;
					_buffer.put(index  , red).put(index+1, green).put(index+2, blue).put(index+3, alpha);
				}
			}
		}
	}

	public long getPixel(final int x, final int y) {
		final int index = y * _width * 4 + x * 4;
		final byte red = _buffer.get(index);
		final byte green = _buffer.get(index+1);
		final byte blue = _buffer.get(index+2);

		return (red << 0x10) | (green << 0x08) | blue;
	}

	public void setPixel(final int x, final int y, final long color) {
		_invalidated = true;
		final int index = y * _width * 4 + x * 4;
		final byte red = (byte)((color & 0xff0000L) >> 0x10);
		final byte green = (byte)((color & 0xff00L) >> 0x08);
		final byte blue = (byte)(color & 0xffL);
		_buffer.put(index  , red);
		_buffer.put(index+1, green);
		_buffer.put(index+2, blue);
		_buffer.put(index+3, (byte)0xff);
	}

	public void dispose() {
		if(-1 != _textureId) {
			synchronized(_textureIdBuffer) {
				_textureIdBuffer.put(0, _textureId);
				GL11.glDeleteTextures(_textureIdBuffer);
			}
		}
	}
	private static int newTextureId() {
		synchronized(_textureIdBuffer) {
			GL11.glGenTextures(_textureIdBuffer);
			return _textureIdBuffer.get(0);
		}
	}

	int JITB$textureId() {
		//TODO optimize using FBO
		if(-1 == _textureId) {
			//
			// Create a new texture for this BitmapData.
			//

			final int id = newTextureId();

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexImage2D(
					GL11.GL_TEXTURE_2D,
					0,
					GL11.GL_RGBA,
					width(),
					height(),
					0,
					GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, _buffer
			);

			_textureId = id;
			_invalidated = false;

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		} else {
			if(_invalidated) {
				//
				// Refresh the texture.
				//

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, _textureId);
				GL11.glTexSubImage2D(
						GL11.GL_TEXTURE_2D,
						0,
						0,
						0,
						width(),
						height(),
						GL11.GL_RGBA,
						GL11.GL_UNSIGNED_BYTE,
						_buffer
				);
				
				_invalidated = false;
			}
		}

		return _textureId;
	}
}
