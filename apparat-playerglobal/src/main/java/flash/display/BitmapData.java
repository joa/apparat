package flash.display;

import flash.filters.BitmapFilter;
import flash.filters.ShaderFilter;
import flash.geom.Point;
import flash.geom.Rectangle;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Joa Ebert
 */
public class BitmapData extends jitb.lang.Object implements IBitmapDrawable {
	private static final Point ORIGIN = new Point();

	public static BitmapData JITB$fromImage(final BufferedImage image) {
		final int width = image.getWidth();
		final int height = image.getHeight();
		final int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

		return new BitmapData(width, height, pixels);
	}
	
	private int _width;
	private int _height;
	private boolean _transparent;
	private Rectangle _rect;

	private boolean _invalidated;
	private ByteBuffer _buffer;
	private int _textureId = -1;

	/**
	 *
	 * @param width
	 * @param height
	 * @param pixels argb
	 */
	private BitmapData(final int width, final int height, final int[] pixels) {
		init(width, height, true);
		final int n = pixels.length;
		int i = 0;
		while(i < n) {
			final int color = pixels[i];
			final byte alpha = (byte)((color & 0xff000000L) >>> 0x18);
			final byte red = (byte)((color & 0xff0000L) >>> 0x10);
			final byte green = (byte)((color & 0xff00L) >>> 0x08);
			final byte blue = (byte)(color & 0xffL);
			_buffer.put(red).put(green).put(blue).put(alpha);
			i++;
		}
		_buffer.flip();
	}

	public BitmapData(final int width, final int height) {
		this(width, height, true);
	}

	public BitmapData(final int width, final int height, final boolean transparent) {
		this(width, height, transparent, 0);
	}

	public BitmapData(final int width, final int height, final boolean transparent, final long fillColor) {
		init(width, height, transparent);
		fillRect(_rect, fillColor);
	}

	private void init(int width, int height, boolean transparent) {
		_width = width;
		_height = height;
		_rect = new Rectangle(0.0, 0.0, _width, _height);
		_transparent = transparent;
		_buffer = BufferUtils.createByteBuffer(width * height * 4);
	}

	public int width() { return _width; }
	public int height() { return _height; }
	public Rectangle rect() { return _rect;	}

	public void applyFilter(final BitmapData sourceBitmapData, final Rectangle sourceRect,
													final Point destPoint, final BitmapFilter filter) {
		final int textureId = JITB$textureId();

		//
		// If this == sourceBitmapData we have to create a temporary buffer
		// as an input since we directly render to this BitmapData.
		//
		// Another case is when the filter is a ShaderFilter and it expects
		// an input which is also set to this.
		//

		final int bufferTextureId;

		if(true) {
			//TODO this can be done in VRAM however LWJGL does not seem to support it?
			bufferTextureId = glGenTextures();
			final ByteBuffer buffer = BufferUtils.createByteBuffer(width()*height()*4);
			buffer.limit(buffer.capacity());
			glBindTexture(GL_TEXTURE_2D, bufferTextureId);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexImage2D(
					GL_TEXTURE_2D,
					0,
					GL_RGBA,
					width(),
					height(),
					0,
					GL_RGBA,
					GL_UNSIGNED_BYTE, buffer);
		} else {
			bufferTextureId = -1;
		}

		//
		// Create and bind FBO
		//

		final int fboId = EXTFramebufferObject.glGenFramebuffersEXT();

		EXTFramebufferObject.glBindFramebufferEXT(
			EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);

		//
		// Attach the current BitmapData as a texture to render to.

		EXTFramebufferObject.glFramebufferTexture2DEXT(
			EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
			EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D,
			-1 == bufferTextureId ? textureId : bufferTextureId, 0);

		//
		// Check the health of our FBO
		//

		final int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(
			EXTFramebufferObject.GL_FRAMEBUFFER_EXT);

		if(status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
			throw new RuntimeException("Could not setup FBO.");
		}

		//
		// Start using FBO
		//

		EXTFramebufferObject.glBindFramebufferEXT(
			EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width(), height());
		
		//
		// Bind the source BitmapData as an input for the filter.
		//

		if(!rect().equals(sourceBitmapData.rect()) || !ORIGIN.equals(destPoint)) {
			glBindTexture(GL_TEXTURE_2D, JITB$textureId());
			rect().JITB$render();
		}

		glPushMatrix();
		glTranslated(destPoint.x, destPoint.y, 0.0);
		glBindTexture(GL_TEXTURE_2D, sourceBitmapData.JITB$textureId());

		//
		// Now run the filter with the given texture.
		//

		if(filter instanceof ShaderFilter) {
			ShaderFilter shaderFilter = ((ShaderFilter)filter);

			if(null != shaderFilter.shader()) {
				shaderFilter.shader().JITB$bind();
			}

			sourceRect.JITB$render();

			if(null != shaderFilter.shader()) {
				shaderFilter.shader().JITB$unbind();
			}
		}

		glPopMatrix();

		//
		// Note: We can defer this step to the next _buffer access.
		//
		
		_buffer.clear();
		glReadPixels(0, 0, width(), height(), GL_RGBA, GL_UNSIGNED_BYTE, _buffer);
		_invalidated = false;

		//
		// Cleanup
		//

		glBindTexture(GL_TEXTURE_2D, 0);
		glPopAttrib();

		EXTFramebufferObject.glBindFramebufferEXT(
			EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

		EXTFramebufferObject.glDeleteFramebuffersEXT(fboId);

		if(-1 != bufferTextureId) {
			//
			// Exchange old texture for buffer.
			//
			
			glDeleteTextures(_textureId);
			_textureId = bufferTextureId;
		}
	}
	
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
			glDeleteTextures(_textureId);
		}
	}

	public int JITB$textureId() {
		if(-1 == _textureId) {
			//
			// Create a new texture for this BitmapData.
			//

			final int id = glGenTextures();

			glBindTexture(GL_TEXTURE_2D, id);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexImage2D(
					GL_TEXTURE_2D,
					0,
					GL_RGBA,
					width(),
					height(),
					0,
					GL_RGBA,
					GL_UNSIGNED_BYTE, _buffer
			);

			_textureId = id;
			_invalidated = false;

			glBindTexture(GL_TEXTURE_2D, 0);
		} else {
			if(_invalidated) {
				//
				// Refresh the texture.
				//

				glBindTexture(GL_TEXTURE_2D, _textureId);
				glTexSubImage2D(
						GL_TEXTURE_2D,
						0,
						0,
						0,
						width(),
						height(),
						GL_RGBA,
						GL_UNSIGNED_BYTE,
						_buffer
				);
				
				_invalidated = false;
			}
		}

		return _textureId;
	}
}
