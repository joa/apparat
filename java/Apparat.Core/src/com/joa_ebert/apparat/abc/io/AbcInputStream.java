/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.abc.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class AbcInputStream extends InputStream
{
	private final InputStream input;

	private long position = 0;
	private long markPosition = 0;

	public AbcInputStream( final InputStream input )
	{
		this.input = input;
	}

	@Override
	public int available() throws IOException
	{
		return input.available();
	}

	@Override
	public void close() throws IOException
	{
		input.close();
	}

	public long getPosition()
	{
		return position;
	}

	@Override
	public synchronized void mark( final int readlimit )
	{
		markPosition = position;

		input.mark( readlimit );
	}

	@Override
	public boolean markSupported()
	{
		return input.markSupported();
	}

	@Override
	public int read() throws IOException
	{
		++position;

		return input.read();
	}

	@Override
	public int read( final byte[] b ) throws IOException
	{
		final int bytesRead = input.read( b );

		position += bytesRead;

		return bytesRead;
	}

	@Override
	public int read( final byte[] b, final int offset, final int length )
			throws IOException
	{
		final int bytesRead = input.read( b, offset, length );

		position += bytesRead;

		return bytesRead;
	}

	public double readD64() throws IOException
	{
		final long first = readU08() | ( readU08() << 8 ) | ( readU08() << 16 )
				| ( readU08() << 24 );
		final long second = readU08() | ( readU08() << 8 ) | ( readU08() << 16 )
				| ( readU08() << 24 );

		return Double.longBitsToDouble( first & 0xFFFFFFFFL | second << 32 );
	}

	public int readS24() throws IOException
	{
		final int byte0 = read();
		final int byte1 = read();
		final int byte2 = read();

		int result = ( byte2 << 0x10 ) | ( byte1 << 0x08 ) | byte0;

		if( 0 != ( result & 0x800000 ) )
		{
			result &= 0x7fffff;
			result -= 0x800000;
		}

		return result;
	}

	public int readS32() throws IOException
	{
		long result = readVariableLength();

		if( 0 != ( result & 0x80000000 ) )
		{
			result &= 0x7fffffff;
			result -= 0x80000000;
		}

		return (int)result;
	}

	public String readString() throws IOException
	{
		final int length = readU30();
		final byte[] bytes = new byte[ length ];

		int offset = 0;

		while( offset < length )
		{
			offset += read( bytes, offset, length - offset );
		}

		return new String( bytes, "UTF8" );
	}

	public int readU08() throws IOException
	{
		return read();
	}

	public int readU16() throws IOException
	{
		final int byte0 = read();
		final int byte1 = read();

		return ( byte1 << 0x08 ) | byte0;
	}

	public int readU30() throws IOException
	{
		return (int)( readVariableLength() & 0x3fffffffL );
	}

	public long readU32() throws IOException
	{
		return readVariableLength();
	}

	private long readVariableLength() throws IOException
	{
		long b = read();
		b &= 0xFF;

		long u32 = b;

		if( !( ( u32 & 0x00000080 ) == 0x00000080 ) )
		{
			return u32;
		}

		b = read();
		b &= 0xFF;

		u32 = u32 & 0x0000007f | b << 7;

		if( !( ( u32 & 0x00004000 ) == 0x00004000 ) )
		{
			return u32;
		}

		b = read();
		b &= 0xFF;

		u32 = u32 & 0x00003fff | b << 14;

		if( !( ( u32 & 0x00200000 ) == 0x00200000 ) )
		{
			return u32;
		}

		b = read();
		b &= 0xFF;

		u32 = u32 & 0x001fffff | b << 21;

		if( !( ( u32 & 0x10000000 ) == 0x10000000 ) )
		{
			return u32;
		}

		b = read();
		b &= 0xFF;

		u32 = u32 & 0x0fffffff | b << 28;

		return u32;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		position = markPosition;

		input.reset();
	}

	@Override
	public long skip( final long n ) throws IOException
	{
		position += n;

		return input.skip( n );
	}
}
