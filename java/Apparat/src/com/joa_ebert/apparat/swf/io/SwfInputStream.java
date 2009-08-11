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

package com.joa_ebert.apparat.swf.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class SwfInputStream extends InputStream
{
	private final InputStream input;

	private int bitBuffer;
	private int bitIndex;

	public SwfInputStream( final InputStream inputStream )
	{
		super();

		this.input = inputStream;

		doBitAlignment();
	}

	private void acquireBits() throws IOException
	{
		bitIndex = 8;
		bitBuffer = read();
	}

	@Override
	public int available() throws IOException
	{
		return input.available();
	}

	@Override
	public void close() throws IOException
	{
		super.close();

		input.close();

		doBitAlignment();
	}

	private boolean currentBitIsTrue() throws IOException
	{
		if( 0 == bitIndex )
		{
			acquireBits();
		}

		return 0 != ( bitBuffer & ( 1 << ( bitIndex - 1 ) ) );
	}

	private void doBitAlignment()
	{
		bitBuffer = 0;
		bitIndex = 0;
	}

	private void goToNextBit()
	{
		--bitIndex;
	}

	@Override
	public synchronized void mark( final int readlimit )
	{
		input.mark( readlimit );
	}

	@Override
	public boolean markSupported()
	{
		return input.markSupported();
	}

	@Override
	public final int read() throws IOException
	{
		return input.read();
	}

	@Override
	public int read( final byte[] b ) throws IOException
	{
		return input.read( b );
	}

	@Override
	public int read( final byte[] b, final int off, final int len )
			throws IOException
	{
		return input.read( b, off, len );
	}

	public float readFIXED() throws IOException
	{
		final float afterDot = readUI16() / 65535.0f;
		final float beforeDot = readUI16();

		return beforeDot + afterDot;
	}

	public float readFIXED8() throws IOException
	{
		final float afterDot = readUI08() / 255.0f;
		final float beforeDot = readUI08();

		return beforeDot + afterDot;
	}

	public RECORDHEADER readRECORDHEADER() throws IOException
	{
		final RECORDHEADER result = new RECORDHEADER();
		final int tagTypeAndLength = readUI16();
		final int tagLength = tagTypeAndLength & 0x3f;

		result.type = tagTypeAndLength >> 6;

		if( tagLength == 0x3f )
		{
			result.length = readSI32();
		}
		else
		{
			result.length = tagLength;
		}

		return result;
	}

	public RECT readRECT() throws IOException
	{
		final RECT result = new RECT();

		final int nBits = readUB( 5 );

		result.minX = readSB( nBits );
		result.maxX = readSB( nBits );
		result.minY = readSB( nBits );
		result.maxY = readSB( nBits );

		return result;
	}

	public RGB readRGB() throws IOException
	{
		final RGB result = new RGB();

		result.red = readUI08();
		result.green = readUI08();
		result.blue = readUI08();

		return result;
	}

	public int readSB( final int n ) throws IOException
	{
		int buffer = 0;

		for( int i = n - 1; i > -1; --i )
		{
			if( currentBitIsTrue() )
			{
				buffer |= 1 << i;
			}

			goToNextBit();
		}

		if( 0 != ( buffer & ( 1 << ( n - 1 ) ) ) )
		{
			buffer &= ( 1 << n ) - 1;
			buffer -= 1 << n;
		}

		return buffer;
	}

	public int readSI08() throws IOException
	{
		doBitAlignment();

		int result = read();

		if( 0 != ( result & 0x80 ) )
		{
			result &= 0x7f;
			result -= 0x80;
		}

		return result;
	}

	public int[] readSI08( final int n ) throws IOException
	{
		final int[] result = new int[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readSI08();
		}

		return result;
	}

	public int readSI16() throws IOException
	{
		doBitAlignment();

		final int byte0 = read();
		final int byte1 = read();

		int result = ( byte1 << 0x08 ) | byte0;

		if( 0 != ( result & 0x8000 ) )
		{
			result &= 0x7fff;
			result -= 0x8000;
		}

		return result;
	}

	public int[] readSI16( final int n ) throws IOException
	{
		final int[] result = new int[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readSI16();
		}

		return result;
	}

	public int readSI32() throws IOException
	{
		doBitAlignment();

		final int byte0 = read();
		final int byte1 = read();
		final int byte2 = read();
		final int byte3 = read();

		long result = ( byte3 << 0x18 ) | ( byte2 << 0x10 ) | ( byte1 << 0x08 )
				| byte0;

		if( 0 != ( result & 0x80000000 ) )
		{
			result &= 0x7fffffff;
			result -= 0x80000000;
		}

		return (int)result;
	}

	public String readSTRING() throws IOException
	{
		//
		// Do we care about SWF Version 1 to 5? Only from version 6 on the
		// Strings are stored using UTF8 encoding.
		//

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int character;

		while( true )
		{
			character = readUI08();

			if( 0 == character )
			{
				break;
			}
			else
			{
				buffer.write( character );
			}
		}

		return new String( buffer.toByteArray(), "UTF8" );
	}

	public ITag readTag() throws IOException
	{
		final RECORDHEADER header = readRECORDHEADER();
		ITag result = null;

		try
		{
			result = Tags.createTag( header.type );
		}
		catch( final InstantiationException e )
		{
			throw new IOException( e );
		}
		catch( final IllegalAccessException e )
		{
			throw new IOException( e );
		}

		try
		{
			result.read( header, this );
		}
		catch( final SwfException e )
		{
			throw new IOException( e );
		}

		return result;
	}

	public int readUB( final int n ) throws IOException
	{
		int buffer = 0;

		for( int i = n - 1; i > -1; --i )
		{
			if( currentBitIsTrue() )
			{
				buffer |= 1 << i;
			}

			goToNextBit();
		}

		return buffer;
	}

	public int readUI08() throws IOException
	{
		doBitAlignment();

		return read();
	}

	public int[] readUI08( final int n ) throws IOException
	{
		final int[] result = new int[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readUI08();
		}

		return result;
	}

	public int readUI16() throws IOException
	{
		doBitAlignment();

		final int byte0 = read();
		final int byte1 = read();

		return ( byte1 << 0x08 ) | byte0;
	}

	public int[] readUI16( final int n ) throws IOException
	{
		final int[] result = new int[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readUI16();
		}

		return result;
	}

	public int readUI24() throws IOException
	{
		doBitAlignment();

		final int byte0 = read();
		final int byte1 = read();
		final int byte2 = read();

		return ( byte2 << 0x10 ) | ( byte1 << 0x08 ) | byte0;
	}

	public int[] readUI24( final int n ) throws IOException
	{
		final int[] result = new int[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readUI24();
		}

		return result;
	}

	public long readUI32() throws IOException
	{
		doBitAlignment();

		final int byte0 = read();
		final int byte1 = read();
		final int byte2 = read();
		final int byte3 = read();

		return ( byte3 << 0x18 ) | ( byte2 << 0x10 ) | ( byte1 << 0x08 )
				| byte0;
	}

	public long[] readUI32( final int n ) throws IOException
	{
		final long[] result = new long[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readUI32();
		}

		return result;
	}

	public BigInteger readUI64() throws IOException
	{
		doBitAlignment();

		final byte[] data = new byte[ 8 ];
		int bytesRead = 0;

		while( bytesRead != 8 )
		{
			bytesRead += read( data );
		}

		for( int i = 0; i < 4; ++i )
		{
			final byte temp = data[ i ];

			data[ i ] = data[ 7 - i ];
			data[ 7 - i ] = temp;
		}

		return new BigInteger( 1, data );
	}

	public BigInteger[] readUI64( final int n ) throws IOException
	{
		final BigInteger[] result = new BigInteger[ n ];

		for( int i = 0; i < n; ++i )
		{
			result[ i ] = readUI64();
		}

		return result;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		input.reset();
	}

	@Override
	public long skip( final long n ) throws IOException
	{
		return input.skip( n );
	}
}
