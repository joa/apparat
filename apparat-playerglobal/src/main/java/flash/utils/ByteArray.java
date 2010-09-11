package flash.utils;

import jitb.errors.MissingImplementationException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Joa Ebert
 */
public class ByteArray extends jitb.lang.Object {
	public static ByteArray JITB$fromBuffer(final ByteBuffer buffer) {
		return new ByteArray(buffer);
	}
	
	public static long defaultObjectEncoding;

	private String _endian;
	private long _objectEncoding;

	private ByteBuffer _buffer;

	public ByteArray() {
		_buffer = ByteBuffer.allocateDirect(0);
		endian(Endian.LITTLE_ENDIAN);
	}

	private ByteArray(final ByteBuffer buffer) {
		_buffer = buffer;
		endian(Endian.LITTLE_ENDIAN);
	}
	
	public long bytesAvailable() {
		return _buffer.capacity() - _buffer.position();
	}

	public String endian() { return _endian; }
	public void endian(final String value) {
		_endian = value;
		_buffer.order(byteOrder());
	}

	private ByteOrder byteOrder() {
		if(_endian.equals(Endian.LITTLE_ENDIAN)) {
			return ByteOrder.LITTLE_ENDIAN;
		} else if(_endian.equals(Endian.BIG_ENDIAN)) {
			return ByteOrder.BIG_ENDIAN;
		} else {
			throw new Error();
		}
	}

	public long length() { return _buffer.capacity(); }
	public void length(final long value) {
		if(value != _buffer.capacity()) {
			final int oldPosition = (int)position();
			final int intValue = (int)value;
			final ByteBuffer oldBuffer = _buffer;
			oldBuffer.position();
			oldBuffer.limit(oldBuffer.capacity());
			_buffer = ByteBuffer.allocateDirect(intValue).order(byteOrder()).put(oldBuffer);
			_buffer.position(oldPosition > intValue ? intValue : oldPosition);
		}
	}

	public long objectEncoding() { return _objectEncoding; }
	public void objectEncoding(final long value) { _objectEncoding = value; }
	
	public long position() { return _buffer.position(); }
	public void position(final long value) {
		_buffer.position((int)value);
	}

	public void clear() {
		_buffer = ByteBuffer.allocateDirect(0).order(byteOrder());
	}

	public void compress(final String algorithm) {
		throw new MissingImplementationException("compress");
	}

	public void deflate() {
		throw new MissingImplementationException("deflate");
	}

	public void inflate() {
		throw new MissingImplementationException("inflate");
	}

	public ByteBuffer JITB$buffer() { return _buffer; }
	public void JITB$buffer(final ByteBuffer value) { _buffer = value; }

	public byte[] JITB$toByteArray() {
		if(_buffer.hasArray()) {
			return _buffer.array();
		}

		final byte[] result = new byte[_buffer.capacity()];
		final int p0 = _buffer.position();

		_buffer.position(0);
		_buffer.limit(_buffer.capacity());
		_buffer.get(result);
		_buffer.position(p0);
		
		return result;
	}

	public java.lang.Object JITB$get(final int index) {
		return _buffer.get(index);
	}

	public void JITB$set(final int index, final int value) {
		if(index > length()) {
			length(index);
		}
		
		_buffer.put(index, (byte)value);
	}

	public void writeFloat(final double value) {
		assureAvailable(4);
		_buffer.asFloatBuffer().put((float)value);
		_buffer.position(_buffer.position()+4);
	}

	public double readFloat() {
		return _buffer.asFloatBuffer().get();
	}

	private void assureAvailable(final int value) {
		if(bytesAvailable() < value) {
			final long newLength = length()+(value - bytesAvailable());
			length(newLength);
		}
	}
}
