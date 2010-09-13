package jitb.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Joa Ebert
 */
public final class ByteBufferInputStream extends InputStream {
	private ByteBuffer buffer;

	public ByteBufferInputStream( ByteBuffer buffer){
		this.buffer = buffer;
	}

	public synchronized int read() throws IOException {
		if (!buffer.hasRemaining()) {
			return -1;
		}
		return buffer.get();
	}
	
	public synchronized int read(byte[] bytes, int off, int len) throws IOException {
		len = Math.min(len, buffer.remaining());
		buffer.get(bytes, off, len);
		return len;
	}
}