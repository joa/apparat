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
import java.io.OutputStream;
import java.math.BigInteger;

import com.joa_ebert.apparat.swf.tags.ITag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class SwfOutputStream extends OutputStream
{
	private final OutputStream output;

	private int bitIndex;
	private int bitBuffer;

	public SwfOutputStream( final OutputStream output )
	{
		this.output = output;
	}

	@Override
	public void close() throws IOException
	{
		super.close();

		output.close();
	}

	private void doBitAlignment() throws IOException
	{
		if( 0 != bitIndex )
		{
			write( bitBuffer );
		}

		bitIndex = 0;
		bitBuffer = 0;
	}

	@Override
	public void flush() throws IOException
	{
		output.flush();
	}

	public void flush( final boolean flushBitBuffer ) throws IOException
	{
		if( flushBitBuffer )
		{
			doBitAlignment();
		}

		flush();
	}

	private void goToNextBit() throws IOException
	{
		++bitIndex;

		if( 8 == bitIndex )
		{
			write( bitBuffer );

			bitIndex = 0;
			bitBuffer = 0;
		}
	}

	private void markCurrentBitTrue()
	{
		bitBuffer |= 1 << ( 7 - bitIndex );
	}

	@Override
	public void write( final byte[] value ) throws IOException
	{
		output.write( value );
	}

	@Override
	public void write( final byte[] value, final int offset, final int length )
			throws IOException
	{
		output.write( value, offset, length );
	}

	@Override
	public void write( final int value ) throws IOException
	{
		output.write( value );
	}

	public void writeFIXED( final float value ) throws IOException
	{
		final int beforeDot = (int)value;
		final int afterDot = (int)( ( value - beforeDot ) * 65535.0f );

		writeUI16( afterDot );
		writeUI16( beforeDot );
	}

	public void writeFIXED8( final float value ) throws IOException
	{
		final int beforeDot = (int)value;
		final int afterDot = (int)( ( value - beforeDot ) * 255.0f );

		writeUI08( afterDot );
		writeUI08( beforeDot );
	}

	public void writeRECORDHEADER( final RECORDHEADER value )
			throws IOException
	{
		int tagTypeAndLength = value.type << 6;
		boolean writeLength = false;

		if( value.length >= 0x3f )
		{
			tagTypeAndLength |= 0x3f;
			writeLength = true;
		}
		else
		{
			tagTypeAndLength |= value.length;
		}

		writeUI16( tagTypeAndLength );

		if( writeLength )
		{
			writeSI32( value.length );
		}
	}

	public void writeRECT( final RECT value ) throws IOException
	{
		final int nBits = value.getNumberOfBits();

		writeUB( nBits, 5 );

		writeSB( value.minX, nBits );
		writeSB( value.maxX, nBits );
		writeSB( value.minY, nBits );
		writeSB( value.maxY, nBits );
	}

	public void writeRGB( final RGB value ) throws IOException
	{
		writeUI08( value.red );
		writeUI08( value.green );
		writeUI08( value.blue );
	}

	public void writeSB( final int value ) throws IOException
	{
		// TODO fix me for negative values

		writeSB( value, 0x21 - Integer.numberOfLeadingZeros( value ) );
	}

	public void writeSB( final int value, final int n ) throws IOException
	{
		// TODO implement me for negative values

		if( value < 0 )
		{
			throw new IOException( "Negative values are currently unsupported." );
		}

		for( int i = n - 1; i > -1; --i )
		{
			if( 0 != ( value & ( 1 << i ) ) )
			{
				markCurrentBitTrue();
			}

			goToNextBit();
		}
	}

	public void writeSI08( final int value ) throws IOException
	{
		doBitAlignment();

		write( value & 0xff );
	}

	public void writeSI08( final int[] value ) throws IOException
	{
		for( final int i : value )
		{
			writeSI08( i );
		}
	}

	public void writeSI16( final int value ) throws IOException
	{
		doBitAlignment();

		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;

		write( byte0 );
		write( byte1 );
	}

	public void writeSI16( final int[] value ) throws IOException
	{
		for( final int i : value )
		{
			writeSI16( i );
		}
	}

	public void writeSI32( final int value ) throws IOException
	{
		doBitAlignment();

		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;
		final int byte2 = ( value & 0xff0000 ) >> 0x10;
		final int byte3 = ( value & 0xff000000 ) >> 0x18;

		write( byte0 );
		write( byte1 );
		write( byte2 );
		write( byte3 );
	}

	public void writeSTRING( final String value ) throws IOException
	{
		//
		// This implementation is only correct for SWF 6 and later.
		// See SwfInputStream#readString.
		// 

		final byte[] buffer = value.getBytes( "UTF8" );

		for( final byte character : buffer )
		{
			writeUI08( character );
		}

		writeUI08( 0x00 );
	}

	public void writeTag( final ITag value ) throws IOException
	{
		final RECORDHEADER header = new RECORDHEADER();

		header.type = value.getType();

		if( !value.isLengthKnown() )
		{
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			final SwfOutputStream swfOutput = new SwfOutputStream( buffer );

			value.write( swfOutput );

			swfOutput.flush( true );

			final byte[] bytes = buffer.toByteArray();

			swfOutput.close();

			header.length = bytes.length;

			writeRECORDHEADER( header );
			write( bytes );
		}
		else
		{
			header.length = value.getLength();

			writeRECORDHEADER( header );

			value.write( this );
		}
	}

	public void writeUB( final int value ) throws IOException
	{
		writeUB( value, 0x20 - Integer.numberOfLeadingZeros( value ) );
	}

	public void writeUB( final int value, final int n ) throws IOException
	{
		for( int i = n - 1; i > -1; --i )
		{
			if( 0 != ( value & ( 1 << i ) ) )
			{
				markCurrentBitTrue();
			}

			goToNextBit();
		}
	}

	public void writeUI08( final int value ) throws IOException
	{
		writeSI08( value );
	}

	public void writeUI08( final int[] value ) throws IOException
	{
		writeSI08( value );
	}

	public void writeUI16( final int value ) throws IOException
	{
		writeSI16( value );
	}

	public void writeUI16( final int[] value ) throws IOException
	{
		writeSI16( value );
	}

	public void writeUI24( final int value ) throws IOException
	{
		doBitAlignment();

		final int byte0 = value & 0xff;
		final int byte1 = ( value & 0xff00 ) >> 0x08;
		final int byte2 = ( value & 0xff0000 ) >> 0x10;

		write( byte0 );
		write( byte1 );
		write( byte2 );
	}

	public void writeUI24( final int[] value ) throws IOException
	{
		for( final int i : value )
		{
			writeUI24( i );
		}
	}

	public void writeUI32( final long value ) throws IOException
	{
		writeSI32( (int)( value & 0xffffffffL ) );
	}

	public void writeUI32( final long[] value ) throws IOException
	{
		for( final long l : value )
		{
			writeUI32( l );
		}
	}

	public void writeUI64( final BigInteger value ) throws IOException
	{
		doBitAlignment();

		final byte data[] = value.toByteArray();

		for( int i = 0; i < 4; ++i )
		{
			final byte temp = data[ i ];

			data[ i ] = data[ 7 - i ];
			data[ 7 - i ] = temp;
		}

		write( data );
	}

	public void writeUI64( final BigInteger[] value ) throws IOException
	{
		for( final BigInteger bi : value )
		{
			writeUI64( bi );
		}
	}

	public void writeUI64( final int value ) throws IOException
	{
		writeUI32( value );
		writeUI32( 0 );
	}

	public void writeUI64( final long value ) throws IOException
	{
		writeUI32( value );
		writeUI32( 0 );
	}

	public void writeUUID( final UUID value ) throws IOException
	{
		final int n = value.hash.length;

		for( int i = 0; i < n; ++i )
		{
			writeUI08( value.hash[ i ] & 0xff );
		}
	}

}
