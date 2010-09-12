package flash.utils;

import jitb.errors.MissingImplementationException;

import java.nio.*;

/**
 * @author Joa Ebert
 */
public class ByteArray extends jitb.lang.Object {
	private static final int INITIAL_SIZE = 0x1000;

	public static ByteArray JITB$fromBuffer(final ByteBuffer buffer) {
		return new ByteArray(buffer);
	}
	
	public static long defaultObjectEncoding;

	private String _endian;
	private long _objectEncoding;

	private int _bytesAvailable;
	private int _position;
	private int _length;

	private ByteBuffer _buffer;

	public ByteArray() {
		_buffer = ByteBuffer.allocateDirect(INITIAL_SIZE);
		_buffer.limit(_buffer.capacity());
		_position = 0;
		_length = 0;
		_bytesAvailable = 0;
		endian(Endian.LITTLE_ENDIAN);
	}

	private ByteArray(final ByteBuffer buffer) {
		_buffer = buffer;
		_position = 0;
		_length = _buffer.capacity();
		_bytesAvailable = _length;
		_buffer.limit(_length);
		_buffer.position(_position);
		endian(Endian.LITTLE_ENDIAN);
	}
	
	public long bytesAvailable() { return _bytesAvailable; }

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

	public long length() { return _length; }
	public void length(final long value) {
		final int intValue = (int)value;
		_length = intValue;

		if(0 == intValue) {
			_buffer = ByteBuffer.allocateDirect(INITIAL_SIZE);
			_buffer.limit(_buffer.capacity());
			_position = 0;
			_bytesAvailable = 0;
		} else if(intValue > _buffer.capacity()) {
			final ByteBuffer oldBuffer = _buffer;

			oldBuffer.position(0);
			oldBuffer.limit(oldBuffer.capacity());

			_buffer = ByteBuffer.allocateDirect(Math.max(INITIAL_SIZE, intValue)).
				order(byteOrder()).put(oldBuffer);
			_buffer.limit(_buffer.capacity());
			_position = _position > intValue ? intValue : _position;
			_bytesAvailable = _length - _position;
		}
	}

	public long objectEncoding() { return _objectEncoding; }
	public void objectEncoding(final long value) { _objectEncoding = value; }
	
	public long position() { return _position; }
	public void position(final long value) {
		assert value <= _length : "Position must not exceed length.";
		_position = (int)value;
		_bytesAvailable = _length - _position;
	}

	public void clear() {
		_buffer = ByteBuffer.allocateDirect(INITIAL_SIZE).order(byteOrder());
		_buffer.limit(_buffer.capacity());
		_position = 0;
		_length = 0;
		_bytesAvailable = 0;
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

	public void writeFloat(final double value) {
		growBy(4);

		_buffer.position(_position);
		_buffer.putFloat((float)value);

		advanceBy(4);
	}

	public double readFloat() {
		final float result;

		_buffer.position(_position);
		result = _buffer.getFloat();

		advanceBy(4);

		return result;
	}

	/**
	 * Advances the internal pointer by a given amount of bytes.
	 *
	 * @param numBytes The number of bytes to advance the pointer.
	 */
	private void advanceBy(final int numBytes) {
		_position += numBytes;
		_bytesAvailable = _length - _position;

		assert _bytesAvailable >= 0 : "Bytes available must not turn negative.";
		assert _position <= _length : "Position must not exceed length.";
	}

	/**
	 * Grows the internal buffer by an amount of required bytes.
	 *
	 * <p>The new buffer size is calculated using the formula
	 * <code>(oldLength + (numBytes - bytesAvailable)) * 3 / 2</code> which
	 * means that the ByteArray can grow larger than the required amount.</p>
	 * 
	 * @param numBytes The number of required bytes.
	 */
	private void growBy(final int numBytes) {
		//
		//               /    FREE    \
		// #############################
		//     ^         ^             ^
		//     POSITION  LENGTH        CAPACITY
		//     \ AVAILABLE /
		//     \   AVAILABLE IN BUFFER   /
		//

		final int available = _length - _position;

		if(available >= numBytes) {
			return;
		}
		
		final int availableInBuffer = _buffer.capacity() - _position;
		final int length = _length;

		if(availableInBuffer < numBytes) {
			final int delta = numBytes - availableInBuffer;
			final int newLength = ((_buffer.capacity() + delta) * 3) >>> 1;

			length(newLength);
		}

		_length = length + numBytes - available;
		_bytesAvailable = _length - _position;
	}

	public ByteBuffer JITB$buffer() {
		_buffer.position(_position);
		_buffer.limit(_buffer.capacity());

		return _buffer;
	}

	public void JITB$buffer(final ByteBuffer value) {
		_buffer = value;
		_length = _buffer.capacity();
		JITB$synchronizeBuffer();
	}

	/**
	 * Synchronizes the position of the internal buffer.
	 */
	public void JITB$synchronizeBuffer() {
		_position = _buffer.position();
		_bytesAvailable = _length - _position;
	}

	public byte[] JITB$toByteArray() {
		final byte[] result = new byte[_length];

		_buffer.position(0);
		_buffer.limit(_buffer.capacity());
		_buffer.get(result);

		return result;
	}

	public java.lang.Object JITB$get(final int index) {
		return _buffer.get(index);
	}

	public void JITB$set(final int index, final int value) {
		if(index > length()) {
			length(index+1);
		}

		_buffer.put(index, (byte)value);
	}
}
